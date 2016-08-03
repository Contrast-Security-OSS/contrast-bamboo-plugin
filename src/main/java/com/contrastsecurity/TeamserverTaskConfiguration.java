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

    private static final String[] SEVERITIES = {"None", "Note", "Low", "Medium", "High", "Critical"};
    private static final String[] TYPES = {"authorization-missing-deny", "authorization-rules-misordered",
            "autocomplete-missing", "cache-control-disabled", "cache-controls-missing", "clickjacking-control-missing",
            "cmd-injection", "compilation-debug", "cookie-flags-missing", "crypto-bad-ciphers", "crypto-bad-mac",
            "crypto-weak-randomness", "csp-header-insecure", "csp-header-missing", "csrf", "custom-errors-off",
            "escape-templates-off", "event-validation-disabled", "expression-language-injection",
            "forms-auth-protection", "forms-auth-redirect", "forms-auth-ssl", "hardcoded-key", "hardcoded-password",
            "header-checking-disabled", "header-injection", "hql-injection", "hsts-header-missing",
            "http-only-disabled", "httponly", "insecure-auth-protocol", "insecure-jsp-access", "ldap-injection",
            "log-injection", "max-request-length", "nosql-injection", "parameter-pollution", "path-traversal",
            "plaintext-conn-strings", "reflected-xss", "reflection-injection", "request-validation-control-disabled",
            "request-validation-disabled", "role-manager-protection", "role-manager-ssl", "secure-flag-missing",
            "session-regenerate", "session-rewriting", "session-timeout", "spring-unchecked-autobinding",
            "sql-injection", "stored-xss", "trace-enabled", "trace-enabled-aspx", "trust-boundary-violation",
            "unsafe-code-execution", "unsafe-readlin", "unsafe-xml-decode", "untrusted-deserializatio",
            "unvalidated-forward", "unvalidated-redirect", "verb-tampering", "version-header-enabled",
            "viewstate-encryption-disabled", "viewstate-mac-disabled", "wcf-detect-replays", "wcf-exception-details",
            "wcf-metadata-enabled", "weak-membership-config", "xcontenttype-header-missing", "xpath-injection", "xxe",
            "xxssprotection-header-disabled"};

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public TeamserverTaskConfiguration(PluginSettingsFactory psf)
    {
        this.pluginSettingsFactory = psf;
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
                                                     @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put("profile_select", params.getString("profile_select"));
        config.put("count", Integer.toString(params.getInt("count", 0)));
        config.put("severity_select", params.getString("severity_select"));
        config.put("type_select", params.getString("type_select"));
        config.put("app_name", params.getString("app_name"));

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
        context.put("severities", SEVERITIES);
        context.put("types", TYPES);

        context.put("profile_select", "");
        context.put("count", 0);
        context.put("severity_select", "");
        context.put("type_select", "");
        context.put("app_name", "");

    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context,
                                       @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        //Gets Profile names from plugin settings
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,Object> map = (Map<String, Object>)settings.get(PLUGIN_PROFILES_KEY);
        String[] profiles = new String[0];

        if (map != null) {
            Set<String> keys = map.keySet();
            profiles = keys.toArray(new String[keys.size()]);
        }

        //Puts the profiles, severities, and types in context for the Freemarker template
        context.put("profiles", profiles);
        context.put("severities", SEVERITIES);
        context.put("types", TYPES);

        context.put("profile_select", taskDefinition.getConfiguration().get("profile_select"));
        context.put("count", taskDefinition.getConfiguration().get("count"));
        context.put("severity_select", taskDefinition.getConfiguration().get("severity_select"));
        context.put("type_select", taskDefinition.getConfiguration().get("type_select"));
        context.put("app_name", taskDefinition.getConfiguration().get("app_name"));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context,
                                       @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);

        context.put("count", taskDefinition.getConfiguration().get("count"));
        context.put("severity_select", taskDefinition.getConfiguration().get("severity_select"));
        context.put("type_select", taskDefinition.getConfiguration().get("type_select"));
        context.put("app_name", taskDefinition.getConfiguration().get("app_name"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);
    }
}