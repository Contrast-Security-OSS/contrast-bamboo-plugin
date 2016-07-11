package com.contrastsecurity;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.contrastsecurity.sdk.ContrastSDK;

@Named("configuration")
@Path("/")
public class ConfigResource
{
	private static final String PLUGIN_STORAGE_KEY = "com.contrastsecurity";
	private static final String PLUGIN_PROFILES_KEY = PLUGIN_STORAGE_KEY + ".profiles";

	@ComponentImport
	private final UserManager userManager;
	@ComponentImport
	private final PluginSettingsFactory pluginSettingsFactory;
	@ComponentImport
	private final TransactionTemplate transactionTemplate;

	@Inject
	public ConfigResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory, 
			TransactionTemplate transactionTemplate)
	{
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response get(@Context HttpServletRequest request)
	{
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.ok(transactionTemplate.execute(new TransactionCallback<Object>()
		{
			public Object doInTransaction()
			{
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
				Map<String, TeamserverProfile> profiles = (Map<String, TeamserverProfile>)settings.get(PLUGIN_PROFILES_KEY);
				return profiles;
			}
		})).build();
	}
	
	@Path("/verifyconnection")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public Response testConnection(final TeamserverProfile profile, @Context HttpServletRequest request)
	{
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		try {
			ContrastSDK contrastsdk = new ContrastSDK(profile.getUsername(), profile.getApikey(), profile.getServicekey());
			int response = contrastsdk.makeConnection(profile.url+"/api/ng/profile", "GET").getResponseCode();
			if(response != 200){
				return Response.serverError().build();
			}
		} catch (IllegalArgumentException e){
			return Response.serverError().build();
		} catch (IOException e) {
			return Response.serverError().build();
		}

		return Response.ok().build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateConfig(final TeamserverProfile profile, @Context HttpServletRequest request)
	{
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback<Object>()
		{
			public Object doInTransaction()
			{
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				Map<String, TeamserverProfile> profiles = (Map<String, TeamserverProfile>)settings.get(PLUGIN_PROFILES_KEY);
				if(profiles == null){
					profiles = new TreeMap<String, TeamserverProfile>();
					System.out.println("profiles was null in post method");
				}
				profiles.put(profile.getProfilename(), profile);
				
				settings.put(PLUGIN_PROFILES_KEY, profiles);

				return null;
			}
		});
		return Response.noContent().build();
	}
}