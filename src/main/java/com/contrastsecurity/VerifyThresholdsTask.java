package com.contrastsecurity;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.FilterForm;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.UrlBuilder;
import com.contrastsecurity.models.*;
import com.contrastsecurity.sdk.ContrastSDK;
import com.opensymphony.xwork2.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VerifyThresholdsTask implements TaskType {

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public VerifyThresholdsTask(PluginSettingsFactory psf) {
        this.pluginSettingsFactory = psf;
    }

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {

        //Get Task related objects
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final ConfigurationMap confmap = taskContext.getConfigurationMap();

        //Get configuration data from task context
        String profile_name = confmap.get("profile_select");
        int thresholdCount = Integer.parseInt(confmap.get("count"));
        String severity = confmap.get("severity_select");
        String vulnType = confmap.get("type_select");
        String app_name = confmap.get("app_name");

        Traces traces;
        Set<Trace> resultTraces = new HashSet<Trace>();

        //Use the pluginsettingsFactory to grab TeamServer profiles
        Map<String, TeamServerProfile> profiles = (Map<String, TeamServerProfile>)(pluginSettingsFactory
                .createGlobalSettings().get(ConfigResource.PLUGIN_PROFILES_KEY));

        //Checks if these profiles are null, fails the build if they are.
        if (profiles == null) {
            buildLogger.addBuildLogEntry("Unable to load TeamServer Profiles. Check on the TeamServer Profiles page that your profiles are configured correctly.");
            return builder.failed().build();
        }

        //Gets relevant teamserver profile from profiles.
        TeamServerProfile profile = profiles.get(profile_name);

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

            int vulnTypeCount = 0; // used for vuln type

            FilterForm filterForm = new FilterForm();
            filterForm.setSeverities(UrlBuilder.getSeverityList(severity));

            traces = contrast.getTraceFilterByRule(profile.getUuid(), applicationId, vulnType, filterForm);

            for (Trace trace: traces.getTraces()) {
                if (trace.getRule().equals(vulnType)) {
                    vulnTypeCount += 1;
                }
            }

            if (vulnTypeCount > thresholdCount) {
                buildLogger.addBuildLogEntry("Failed on the threshold condition where the count is " + thresholdCount +
                                            ", severity is " + severity +
                                            ", and rule type is " + vulnType);

                return builder.failed().build();
            }

            return builder.success().build();
        } catch (IOException e) {
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
}
