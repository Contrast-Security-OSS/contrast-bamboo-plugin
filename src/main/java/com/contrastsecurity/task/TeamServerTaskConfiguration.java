package com.contrastsecurity.task;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.contrastsecurity.model.TeamServerProfile;
import com.contrastsecurity.model.Threshold;
import com.contrastsecurity.model.VulnerabilityTypes;
import com.opensymphony.xwork2.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamServerTaskConfiguration extends AbstractTaskConfigurator
{
    private static final String[] SEVERITIES = VulnerabilityTypes.SEVERITIES;
    private static final String[] TYPES = VulnerabilityTypes.TYPES;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public TeamServerTaskConfiguration(PluginSettingsFactory psf)
    {
        this.pluginSettingsFactory = psf;
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
                                                     @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        int total_thresholds = 0;

        for (String key : params.keySet()) {
            if (key.startsWith("count_")) {
                total_thresholds++;
            }
        }

        config.put("profile_select", params.getString("profile_select"));
        config.put("app_name", params.getString("app_name"));


        int current = 1;
        for(int i = 1; i <= total_thresholds && i < 100; i++){
            for( ; !params.containsKey("count_" + current); current++);

            config.put("count_" + i, Integer.toString(params.getInt("count_" + current, 0)));
            config.put("severity_select_" + i, params.getString("severity_select_" + current));
            config.put("type_select_" + i, params.getString("type_select_" + current));
            current++;
        }
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,Object> map = (Map<String, Object>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);
        String[] profiles = new String[0];

        if (map != null) {
            Set<String> keys = map.keySet();
            profiles = keys.toArray(new String[keys.size()]);
        }

        context.put("profiles", profiles);
        context.put("severities", SEVERITIES);
        context.put("types", TYPES);

        context.put("profile_select", "");
        context.put("app_name", "");

        context.put("thresholds", Arrays.asList(new Threshold(0, "","")));
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context,
                                       @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        //Gets Profile names from plugin settings
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,Object> map = (Map<String, Object>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);
        Set<String> profiles = new HashSet<String>();

        if (map != null) {
            profiles = map.keySet();
        }

        //Puts the profiles, severities, and types in context for the Freemarker template
        context.put("profiles", profiles);
        context.put("severities", SEVERITIES);
        context.put("types", TYPES);

        context.put("profile_select", taskDefinition.getConfiguration().get("profile_select"));
        context.put("app_name", taskDefinition.getConfiguration().get("app_name"));

        ArrayList<Threshold> thresholds = new ArrayList<Threshold>();

        for(int i = 1; ; i++){
            if(!taskDefinition.getConfiguration().containsKey("count_" + i)){
                break;
            }
            thresholds.add(new Threshold(
                    Integer.parseInt(taskDefinition.getConfiguration().get("count_" + i)),
                    taskDefinition.getConfiguration().get("severity_select_" + i),
                    taskDefinition.getConfiguration().get("type_select_" + i)));
        }

        context.put("thresholds", thresholds);
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context,
                                       @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);

        context.put("app_name", taskDefinition.getConfiguration().get("app_name"));
        context.put("count", taskDefinition.getConfiguration().get("count_1"));
        context.put("severity_select", taskDefinition.getConfiguration().get("severity_select_1"));
        context.put("type_select", taskDefinition.getConfiguration().get("type_select_1"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

    }
}