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
import com.contrastsecurity.data.TeamServerProfile;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.model.Finding;
import com.contrastsecurity.model.Threshold;
import com.contrastsecurity.models.*;
import com.contrastsecurity.sdk.ContrastSDK;
import com.contrastsecurity.util.KeyGenerator;
import com.google.inject.Inject;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;


public class VerifyThresholdsTask implements TaskType {

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final ActiveObjects activeObjects;
    public static final String DATA_STORAGE_CONTRAST = "com.contrastsecurity.bambooplugin:"; //append build ids for a storage key
    private final ArrayList<Finding> findings = new ArrayList<Finding>();
    private static final HashMap<String, Integer> SEVERITIES = new HashMap<String, Integer>();

    static{
        SEVERITIES.put("Any", -1);
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
        String server_name = confmap.get("server_name");
        String app_name = confmap.get("app_name");
        boolean passive = confmap.getAsBoolean("passive");


        final String key = DATA_STORAGE_CONTRAST + getReportAccessibleKey(taskContext.getBuildContext().getEntityKey().getKey()) + taskContext.getBuildContext().getBuildNumber();

        ArrayList<Threshold> thresholds = loadThresholds(confmap);

        //Use the pluginsettingsFactory to grab TeamServer profiles
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String,TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);

        //Checks if these profiles are null, fails the build if they are.
        if(!verifySettings(profiles, buildLogger, profile_name)){
            return builder.failed().build();
        }

        TeamServerProfile profile = profiles.get(profile_name);

        ContrastSDK contrast = new ContrastSDK(profile.getUsername(), profile.getServicekey(), profile.getApikey(), profile.getUrl());

        try {
            //Get app and server id
            String applicationId = getApplicationId(contrast, profile.getUuid(), app_name);
            List<Long> serverIds = null;
            if (StringUtils.isNotBlank(server_name)) {
                serverIds = getServerId(contrast, profile.getUuid(), server_name, applicationId);
            }

            for(Threshold condition: thresholds) {
                int maxVulns = condition.getCount();
                String type = condition.getType_select();
                String severity = condition.getSeverity_select();
                //can create result here

                buildLogger.addBuildLogEntry("Verifying conditions for app " + app_name + ", id " + applicationId);

                buildLogger.addBuildLogEntry("Attempting the threshold condition where the count is " + maxVulns + ", severity is " + severity + ", and rule type is " + type);
                
                if(passive) {
                    buildLogger.addBuildLogEntry("Passive mode is on. We won't use an appVersion to check for vulnerabilities");
                } else {
                    buildLogger.addBuildLogEntry("Passive mode is off.");
                }

                int vulnTypeCount = 0; // used for vuln type

                com.contrastsecurity.http.TraceFilterForm filterForm = new TraceFilterForm();

                if (!passive) {
                    buildLogger.addBuildLogEntry("and app version is " + buildAppVersionTag(app_name, taskContext.getBuildContext().getBuildNumber()));
                    filterForm.setAppVersionTags(Collections.singletonList(buildAppVersionTag(app_name, taskContext.getBuildContext().getBuildNumber())));
                }

                if(!"Any".equals(type)){
                    filterForm.setVulnTypes(Arrays.asList(type));
                }

                if(!"Any".equals(severity)){
                    filterForm.setSeverities(getSeverityList(severity));
                }


                if (serverIds == null) {
                    buildLogger.addBuildLogEntry("Not filtering on server name");
                } else {
                    buildLogger.addBuildLogEntry("Server name " + server_name + " with ids " + serverIds);
                    filterForm.setServerIds(serverIds);
                }

                Traces traces;
                if (!passive) {
                    traces = contrast.getTracesInOrg(profile.getUuid(), filterForm);
                } else {
                    traces = contrast.getTraces(profile.getUuid(), applicationId, filterForm);
                }

                for (final Trace trace : traces.getTraces()) {
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

                buildLogger.addBuildLogEntry("\tThere were " + vulnTypeCount + " vulns of this type of " + traces.getCount() + " total");
                if (traces.getCount() > maxVulns) {
                    buildLogger.addBuildLogEntry("Failed on the threshold condition where the minimum threshold is " + maxVulns +
                            ", severity is " + severity +
                            ", and rule type is " + type);
                    return builder.failed().build();
                }
            }

            return builder.success().build();
        } catch (IOException e) {
            buildLogger.addBuildLogEntry("IOException");
            e.printStackTrace();
            buildLogger.addBuildLogEntry(e.getMessage());
            return builder.failed().build();
        } catch (UnauthorizedException e){
            buildLogger.addBuildLogEntry("Unable to connect to Contrast. " + e.getMessage());
            return builder.failed().build();
        }
    }


    private List<Long> getServerId(ContrastSDK sdk, String organizationUuid, String serverName, String applicationId) throws IOException {
        ServerFilterForm serverFilterForm = new ServerFilterForm();
        serverFilterForm.setApplicationIds(Arrays.asList(applicationId));
        serverFilterForm.setQ(URLEncoder.encode(serverName, "UTF-8"));

        Servers servers;
        List<Long> serverIds;

        try {
            servers = sdk.getServersWithFilter(organizationUuid, serverFilterForm);
        } catch (IOException e) {
            throw new IOException("Unable to retrieve the servers. " + e.getMessage());
        } catch (UnauthorizedException e) {
            throw new IOException("Unable to connect to Contrast.");
        }

        if (!servers.getServers().isEmpty()) {
            serverIds = servers.getServers().stream().map(s -> s.getServerId()).collect(Collectors.toList());
        } else {
            throw new IOException("Server with name '" + serverName + "' not found.");
        }

        return serverIds;
    }
    private String getApplicationId(ContrastSDK sdk, String organizationUuid, String applicationName) throws IOException{

        Applications applications;

        try {
            applications = sdk.getApplications(organizationUuid);
        } catch (IOException e) {
            throw new IOException("Unable to retrieve the applications. " + e.getMessage());
        } catch (UnauthorizedException e) {
            throw new IOException("Unable to connect to Contrast.");
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
            buildLogger.addBuildLogEntry("Unable to load Contrast Profiles. Check on the Contrast Profiles page that your profiles are configured correctly.");
            return false;
        }

        //Gets relevant teamserver profile from profiles.
        TeamServerProfile profile = profiles.get(profileName);
        if(profile == null) {
            buildLogger.addBuildLogEntry("Unable to load Contrast Profile " + profileName + ". Check on the Contrast Profiles page that this profile is configured correctly.");
            return false;
        }

        if (profile.getUuid() == null) {
            buildLogger.addBuildLogEntry("An organization id must be configured to check for vulnerabilities.");
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

    public static String buildAppVersionTag(String applicationName, int buildNumber) {
        return applicationName + "-" + buildNumber;
    }


}
