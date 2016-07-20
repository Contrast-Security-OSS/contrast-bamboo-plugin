package com.contrastsecurity;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class TeamserverTaskConfiguration extends AbstractTaskConfigurator
{
    private static final String PLUGIN_STORAGE_KEY = "com.contrastsecurity";
    private static final String PLUGIN_PROFILES_KEY = PLUGIN_STORAGE_KEY + ".profiles";

    private TextProvider textProvider;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public TeamserverTaskConfiguration(PluginSettingsFactory psf)
    {
        this.pluginSettingsFactory = psf;
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put("count", params.getInt("count", 0) + "");
        config.put("severity", params.getString("severity"));
        config.put("type", params.getString("type"));

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,Object> map = (Map<String, Object>)settings.get(PLUGIN_PROFILES_KEY);
        String[] profiles = new String[0];

        if (map != null) {
            Set<String> keys = map.keySet();
            profiles = keys.toArray(new String[keys.size()]);
        }

        context.put("profiles", profiles);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);

        //context.put("say", taskDefinition.getConfiguration().get("say"));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        //context.put("say", taskDefinition.getConfiguration().get("say"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        /*final String sayValue = params.getString("say");
        if (StringUtils.isEmpty(sayValue))
        {
            errorCollection.addError("say", textProvider.getText("helloworld.say.error"));
        }*/
    }

    public void setTextProvider(final TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}