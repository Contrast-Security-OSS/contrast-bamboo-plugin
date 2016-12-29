package com.contrastsecurity;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.bandana.BambooBandanaManager;
import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.bandana.DefaultBandanaManager;
import com.atlassian.bandana.impl.MemoryBandanaPersister;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.FilterForm;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.UrlBuilder;
import com.contrastsecurity.models.*;
import com.contrastsecurity.sdk.ContrastSDK;
import com.opensymphony.xwork2.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.ManagerFactoryParameters;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyThresholdsTask implements TaskType {

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final ActiveObjects activeObjects;

    private final String DATA_STORAGE_CONTRAST = "com.contrastsecurity.bambooplugin:"; //append build ids for a storage key
    private final ArrayList<Finding> findings = new ArrayList<Finding>();

    @Inject
    public VerifyThresholdsTask(PluginSettingsFactory psf, ActiveObjects activeObjects) {
        this.pluginSettingsFactory = psf;
        this.activeObjects = activeObjects;
    }

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final ConfigurationMap confmap = taskContext.getConfigurationMap();

        //Get configuration data from task context
        String profile_name = confmap.get("profile_select");
        String app_name = confmap.get("app_name");
        final String key = DATA_STORAGE_CONTRAST + getReportAccessibleKey(taskContext.getBuildContext().getEntityKey().getKey()) + taskContext.getBuildContext().getBuildNumber();//PROJ-PLAN-#
        ArrayList<Threshold> thresholds = new ArrayList<Threshold>();
        for(int i = 1; ; i++){
            if(!confmap.containsKey("count_" + i)){
                break;
            }
            thresholds.add(new Threshold(Integer.parseInt(confmap.get("count_" + i)), confmap.get("severity_select_" + i), confmap.get("type_select_" + i)));
        }

        //Use the pluginsettingsFactory to grab TeamServer profiles
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);

        //Checks if these profiles are null, fails the build if they are.
        if(!verifySettings(profiles, buildLogger, profile_name)){
            return builder.failed().build();
        }
        TeamServerProfile profile = profiles.get(profile_name);

        ContrastSDK contrast = new ContrastSDK(profile.getUsername(), profile.getApikey(), profile.getServicekey(), profile.getUrl());
        try {

            String applicationId = getApplicationId(contrast, profile.getUuid(), app_name);
            long serverId = getServerId(contrast, profile.getUuid(), profile.getServerName(), applicationId);

            for(Threshold condition: thresholds) {
                int maxVulns = condition.getCount();
                String type = condition.getType_select();
                String severity = condition.getSeverity_select();
                //can create result here

                buildLogger.addBuildLogEntry("Attempting the threshold condition where the count is " + maxVulns + ", severity is " + severity + ", and rule type is " + type);

                int vulnTypeCount = 0; // used for vuln type

                FilterForm filterForm = new FilterForm();

                if (!severity.equals("None")) {
                    filterForm.setSeverities(UrlBuilder.getSeverityList(severity));
                } else {
                    filterForm = null;
                }

                //if (type.equals("None")) {
                Traces traces = contrast.getTracesWithFilter(profile.getUuid(), applicationId, "servers", Long.toString(serverId), filterForm);
                //    vulnTypeCount = traces.getCount();
                //} else {
                //    traces = contrast.getTraceFilterByRule(profile.getUuid(), applicationId, type, filterForm);


                for (final Trace trace : traces.getTraces()) {
                    if (trace.getRule().equals(type) || type.equals("None")){
                        activeObjects.executeInTransaction(new TransactionCallback<Finding>(){
                            public Finding doInTransaction() {
                                final Finding result = activeObjects.create(Finding.class);
                                result.setSeverity(trace.getSeverity());
                                result.setType(trace.getRule());
                                result.setBuildId(key);
                                result.save();
                                findings.add(result);
                                return result;
                            }
                        });

                        vulnTypeCount += 1;
                    }
                }
            //}

                buildLogger.addBuildLogEntry("\tThere were " + vulnTypeCount + " vulns of this type of " + traces.getCount() + " total");
                if (vulnTypeCount >= maxVulns) {
                    buildLogger.addBuildLogEntry("Failed on the threshold condition where the minimum threshold is " + maxVulns +
                            ", severity is " + severity +
                            ", and rule type is " + type);
                    return builder.failed().build();
                }
            }
            return builder.success().build();
        } catch (IOException e) {
            buildLogger.addBuildLogEntry("IOException");
            buildLogger.addBuildLogEntry(e.getMessage());
            return builder.failed().build();
        } catch (UnauthorizedException e){
            buildLogger.addBuildLogEntry("Unable to connect to TeamServer. " + e.getMessage());
            return builder.failed().build();
        }
    }


    private long getServerId(ContrastSDK sdk, String organizationUuid, String serverName, String applicationId) throws IOException {
        ServerFilterForm serverFilterForm = new ServerFilterForm();
        serverFilterForm.setApplicationIds(Arrays.asList(applicationId));
        serverFilterForm.setQ(serverName);

        Servers servers;
        long serverId;

        try {
            servers = sdk.getServersWithFilter(organizationUuid, serverFilterForm);
        } catch (IOException e) {
            throw new IOException("Unable to retrieve the servers. " + e.getMessage());
        } catch (UnauthorizedException e) {
            throw new IOException("Unable to connect to TeamServer.");
        }

        if (!servers.getServers().isEmpty()) {
            serverId = servers.getServers().get(0).getServerId();
        } else {
            throw new IOException("Server with name '" + serverName + "' not found.");
        }

        return serverId;
    }
    private String getApplicationId(ContrastSDK sdk, String organizationUuid, String applicationName) throws IOException{

        Applications applications;

        try {
            applications = sdk.getApplications(organizationUuid);
        } catch (IOException e) {
            throw new IOException("Unable to retrieve the applications. " + e.getMessage());
        } catch (UnauthorizedException e) {
            throw new IOException("Unable to connect to TeamServer.");
        }

        for(Application application: applications.getApplications()) {
            if (applicationName.equals(application.getName())) {
                return application.getId();
            }
        }

        throw new IOException("Application with name '" + applicationName + "' not found.");
    }
    private boolean verifySettings(Map<String,TeamServerProfile> profiles, BuildLogger buildLogger, String profileName){

        if (profiles == null) {
            buildLogger.addBuildLogEntry("Unable to load TeamServer Profiles. Check on the TeamServer Profiles page that your profiles are configured correctly.");
            return false;
        }

        //Gets relevant teamserver profile from profiles.
        TeamServerProfile profile = profiles.get(profileName);
        if(profile == null) {
            buildLogger.addBuildLogEntry("Unable to load TeamServer Profile " + profileName + ". Check on the TeamServer Profiles page that this profile is configured correctly.");
            return false;
        }

        if (profile.getUuid() == null) {
            buildLogger.addBuildLogEntry("An organization id must be configured to check for vulnerabilities.");
            return false;
        }

        if (profile.getServerName() == null) {
            buildLogger.addBuildLogEntry("A server name must be configured to check for vulnerabilities.");
            return false;
        }

        return true;
    }

    private String getReportAccessibleKey(String candidate){
        System.out.println("CANDIDATE KEY: " + candidate);
        String re1="((?:[a-z][a-z0-9_]*))(-)((?:[a-z][a-z0-9_]*))(-)";
        Pattern p = Pattern.compile(re1,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(candidate);
        if(m.find()){
            System.out.println("FOUND: " + m.group(1) + m.group(2) + m.group(3) + m.group(4));
            return m.group(1) + m.group(2) + m.group(3) + m.group(4);
        }
        return candidate;

    }

}
