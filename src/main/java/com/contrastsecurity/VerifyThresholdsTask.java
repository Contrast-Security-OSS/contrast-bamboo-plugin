package com.contrastsecurity;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import org.jetbrains.annotations.NotNull;

public class VerifyThresholdsTask implements TaskType{

    @NotNull
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final ConfigurationMap map = taskContext.getConfigurationMap();

        buildLogger.addBuildLogEntry(map.get("count"));
        buildLogger.addBuildLogEntry(map.get("severity"));
        buildLogger.addBuildLogEntry(map.get("type"));

        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }
}
