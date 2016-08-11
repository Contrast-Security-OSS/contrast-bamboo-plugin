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

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.sdk.ContrastSDK;

@Named("configuration")
@Path("/")
public class ConfigResource
{
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
				Map<String, TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);
				return profiles;
			}
		})).build();
	}
	
	@Path("/verifyconnection")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public Response testConnection(final TeamServerProfile profile, @Context HttpServletRequest request)
	{
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}
		try {
			ContrastSDK contrastsdk = new ContrastSDK(profile.getUsername(), profile.getApikey(), profile.getServicekey(), profile.getUrl());
			System.out.println(profile.getUrl());
			contrastsdk.getProfileDefaultOrganizations();
		} catch (UnauthorizedException e){
			return Response.status(Status.FORBIDDEN).build();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("IOException from verify");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok().build();
	}

	@Path("/deleteprofile")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public Response deleteProfile(final TeamServerProfile profile, @Context HttpServletRequest request){
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		Boolean success = (Boolean)transactionTemplate.execute(new TransactionCallback<Object>()
		{
			public Object doInTransaction()
			{
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				Map<String, TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);
				if(profiles == null){
					return false;
				}
				TeamServerProfile fullProfile = profiles.remove(profile.getProfileName());
				settings.put(TeamServerProfile.PLUGIN_PROFILES_KEY, profiles);

				return (fullProfile != null);
			}
		});

		if(success){
			return Response.noContent().build();
		}
		else {
			return Response.notModified().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateConfig(final TeamServerProfile profile, @Context HttpServletRequest request)
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

				Map<String, TeamServerProfile> profiles = (Map<String, TeamServerProfile>)settings.get(TeamServerProfile.PLUGIN_PROFILES_KEY);
				if(profiles == null){
					profiles = new TreeMap<String, TeamServerProfile>();
				}
				profiles.put(profile.getProfileName(), profile);
				
				settings.put(TeamServerProfile.PLUGIN_PROFILES_KEY, profiles);

				return null;
			}
		});
		return Response.noContent().build();
	}
}