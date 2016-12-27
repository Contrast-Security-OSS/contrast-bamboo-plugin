package com.contrastsecurity;

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

public class VerifyThresholdsTask implements TaskType {

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final BandanaManager dataStorage;

    @Inject
    public VerifyThresholdsTask(PluginSettingsFactory psf, BandanaManager dataStorage) {
        System.out.println("CALLED CONSTRUCTOR");
        this.pluginSettingsFactory = psf;
        System.out.println(psf);
        this.dataStorage = dataStorage;
        System.out.println(dataStorage);
    }

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        //dataStorage.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, "key", "value");
        System.out.println((String)dataStorage.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, "key"));
        //dataStorage.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, ,);
        //Get Task related objects
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext); //Initially set to Failed.

        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final ConfigurationMap confmap = taskContext.getConfigurationMap();

        //Get configuration data from task context
        String profile_name = confmap.get("profile_select");
        String app_name = confmap.get("app_name");

        ArrayList<Threshold> thresholds = new ArrayList<Threshold>();
        for(int i = 1; ; i++){
            if(!confmap.containsKey("count_" + i)){
                break;
            }
            thresholds.add(new Threshold(
                    Integer.parseInt(confmap.get("count_" + i)),
                    confmap.get("severity_select_" + i),
                    confmap.get("type_select_" + i)));
        }

        Traces traces;
        Set<Trace> resultTraces = new HashSet<Trace>();

        //Use the pluginsettingsFactory to grab TeamServer profiles
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);

        //Checks if these profiles are null, fails the build if they are.
        if (profiles == null) {
            buildLogger.addBuildLogEntry("Unable to load TeamServer Profiles. Check on the TeamServer Profiles page that your profiles are configured correctly.");
            return builder.failed().build();
        }

        //Gets relevant teamserver profile from profiles.
        TeamServerProfile profile = profiles.get(profile_name);
        if(profile == null) {
            buildLogger.addBuildLogEntry("Unable to load TeamServer Profile " + profile_name + ". Check on the TeamServer Profiles page that this profile is configured correctly.");
            return builder.failed().build();
        }

        if (profile.getUuid() == null) {
            buildLogger.addBuildLogEntry("An organization id must be configured to check for vulnerabilities.");
            return builder.failed().build();
        }

        if (profile.getServerName() == null) {
            buildLogger.addBuildLogEntry("A server name must be configured to check for vulnerabilities.");
            return builder.failed().build();
        }

        ContrastSDK contrast = new ContrastSDK(profile.getUsername(), profile.getApikey(), profile.getServicekey(), profile.getUrl());

        try {

            String applicationId = getApplicationId(contrast, profile.getUuid(), app_name);
            long serverId = getServerId(contrast, profile.getUuid(), profile.getServerName(), applicationId);

            for(Threshold condition: thresholds) {
                int maxVulns = condition.getCount();
                String type = condition.getType_select();
                String severity = condition.getSeverity_select();

                buildLogger.addBuildLogEntry("Attempting the threshold condition where the count is " + maxVulns +
                        ", severity is " + severity +
                        ", and rule type is " + type);


                int vulnTypeCount = 0; // used for vuln type

                FilterForm filterForm = new FilterForm();

                if (!severity.equals("None")) {
                    filterForm.setSeverities(UrlBuilder.getSeverityList(severity));
                } else {
                    filterForm = null;
                }

                if (type.equals("None")) {
                    traces = contrast.getTracesWithFilter(profile.getUuid(), applicationId, "servers", Long.toString(serverId), filterForm);
                    vulnTypeCount = traces.getCount();
                } else {
                    traces = contrast.getTraceFilterByRule(profile.getUuid(), applicationId, type, filterForm);

                    for (Trace trace: traces.getTraces()) {
                        if (trace.getRule().equals(type)) {
                            vulnTypeCount += 1;
                        }
                    }
                }

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

    /**
     * Retrieves the server id by server name
     *
     * @param sdk              Contrast SDK object
     * @param organizationUuid uuid of the organization
     * @param serverName       name of the server to filter on
     * @param applicationId    application id to filter on
     * @return Long id of the server
     */
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

    private void save(String key){
        //String key = taskContext.getBuildContext().getBuildKey().getKey();


    }
}
