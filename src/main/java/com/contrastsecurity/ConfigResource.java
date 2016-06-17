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


import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@Named("configuration")
@Path("/")
public class ConfigResource
{
	private static final String PLUGIN_STORAGE_KEY = "com.contrastsecurity";

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

	//	@XmlRootElement
	//	@XmlAccessorType(XmlAccessType.FIELD)

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
				TeamserverProfile config = new TeamserverProfile();
				config.setUsername((String) settings.get(PLUGIN_STORAGE_KEY + ".username"));				
				config.setApikey((String) settings.get(PLUGIN_STORAGE_KEY + ".apikey"));
				config.setServicekey((String) settings.get(PLUGIN_STORAGE_KEY + ".servicekey"));
				config.setUrl((String) settings.get(PLUGIN_STORAGE_KEY + ".url"));
				return config;
			}
		})).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(final TeamserverProfile config, @Context HttpServletRequest request)
	{
		System.out.println(config);

		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback<Object>()
		{
			public Object doInTransaction()
			{
				PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

				pluginSettings.put(PLUGIN_STORAGE_KEY + ".username", config.getUsername());
				pluginSettings.put(PLUGIN_STORAGE_KEY  +".apikey", config.getApikey());
				pluginSettings.put(PLUGIN_STORAGE_KEY  +".servicekey", config.getServicekey());
				pluginSettings.put(PLUGIN_STORAGE_KEY  +".url", config.getUrl());
				return null;
			}
		});
		return Response.noContent().build();
	}

}