package com.contrastsecurity;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.opensymphony.xwork2.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VerifyThresholdsTask implements TaskType{

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public VerifyThresholdsTask(PluginSettingsFactory psf)
    {
        this.pluginSettingsFactory = psf;
    }

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,TeamserverProfile> profiles = (Map<String, TeamserverProfile>)settings.get(ConfigResource.PLUGIN_PROFILES_KEY);
        if(profiles != null){
            final ConfigurationMap confmap = taskContext.getConfigurationMap();
            buildLogger.addBuildLogEntry(confmap.get("count"));
            buildLogger.addBuildLogEntry(confmap.get("severity"));
            buildLogger.addBuildLogEntry(confmap.get("type"));
            return TaskResultBuilder.newBuilder(taskContext).success().build();
        } else {
            buildLogger.addBuildLogEntry("Unable to load Teamserver Profiles. Check the Teamserver Profiles page that your profiles are configured correctly.");
            return builder.failed().build();
        }
    }
}
