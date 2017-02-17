package com.contrastsecurity.task;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.util.concurrent.NotNull;
import com.contrastsecurity.util.KeyGenerator;
import com.contrastsecurity.model.TeamServerProfile;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.model.Finding;
import com.contrastsecurity.model.Threshold;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.Servers;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.Traces;
import com.contrastsecurity.sdk.ContrastSDK;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class VerifyThresholdsTask implements TaskType {

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final ActiveObjects activeObjects;
    public static final String DATA_STORAGE_CONTRAST = "com.contrastsecurity.bambooplugin:"; //append build ids for a storage key
    private final ArrayList<Finding> findings = new ArrayList<Finding>();
    private static final HashMap<String, Integer> SEVERITIES = new HashMap<String, Integer>();

    static{
        SEVERITIES.put("None", -1);
        SEVERITIES.put("Note", 0);
        SEVERITIES.put("Low", 1);
        SEVERITIES.put("Medium", 2);
        SEVERITIES.put("High", 3);
        SEVERITIES.put("Critical", 4);
    }

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


        final String key = DATA_STORAGE_CONTRAST + getReportAccessibleKey(taskContext.getBuildContext().getEntityKey().getKey()) + taskContext.getBuildContext().getBuildNumber();

        ArrayList<Threshold> thresholds = loadThresholds(confmap);

        //Use the pluginsettingsFactory to grab TeamServer profiles
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        System.out.println(settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY));
        Map<String,TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);

        //Checks if these profiles are null, fails the build if they are.
        if(!verifySettings(profiles, buildLogger, profile_name)){
            return builder.failed().build();
        }

        TeamServerProfile profile = profiles.get(profile_name);

        ContrastSDK contrast = new ContrastSDK(profile.getUsername(), profile.getApikey(), profile.getServicekey(), profile.getUrl());

        try {
            //Get app and server id
            String applicationId = getApplicationId(contrast, profile.getUuid(), app_name);
            long serverId = getServerId(contrast, profile.getUuid(), profile.getServerName(), applicationId);

            for(Threshold condition: thresholds) {
                int maxVulns = condition.getCount();
                String type = condition.getType_select();
                String severity = condition.getSeverity_select();
                //can create result here

                buildLogger.addBuildLogEntry("Attempting the threshold condition where the count is " + maxVulns + ", severity is " + severity + ", and rule type is " + type);

                int vulnTypeCount = 0; // used for vuln type

                com.contrastsecurity.http.TraceFilterForm filterForm = new TraceFilterForm();

                filterForm.setSeverities(getSeverityList(severity));
                filterForm.setServerIds(Arrays.asList(serverId));


                Traces traces = contrast.getTraces(profile.getUuid(), applicationId, filterForm);

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
                //saveBeforeExit(key);
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
            buildLogger.addBuildLogEntry("IOException ");
            e.printStackTrace();
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
    public boolean verifySettings(Map<String,TeamServerProfile> profiles, BuildLogger buildLogger, String
            profileName){

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
        return KeyGenerator.generate(candidate);
    }

    private ArrayList<Threshold> loadThresholds(ConfigurationMap confMap){
        ArrayList<Threshold> thresholds = new ArrayList<Threshold>();
        for(int i = 1; ; i++){
            if(!confMap.containsKey("count_" + i)){
                break;
            }
            thresholds.add(new Threshold(Integer.parseInt(confMap.get("count_" + i)), confMap.get("severity_select_" + i), confMap.get("type_select_" + i)));
        }
        return thresholds;
    }

    public static EnumSet<RuleSeverity> getSeverityList(String severity) {

        List<RuleSeverity> ruleSeverities = new ArrayList<RuleSeverity>();
        switch(SEVERITIES.get(severity)){
            case -1:
            case 0:
                ruleSeverities.add(RuleSeverity.NOTE);
            case 1:
                ruleSeverities.add(RuleSeverity.LOW);
            case 2:
                ruleSeverities.add(RuleSeverity.MEDIUM);
            case 3:
                ruleSeverities.add(RuleSeverity.HIGH);
            case 4:
                ruleSeverities.add(RuleSeverity.CRITICAL);
        }

        return EnumSet.copyOf(ruleSeverities);
    }


}
