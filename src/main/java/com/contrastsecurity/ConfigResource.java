package com.contrastsecurity;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

@Path("/")
public class ConfigResource
{
	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	public ConfigResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory, 
			TransactionTemplate transactionTemplate)
	{
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}
	@SuppressWarnings({ "rawtypes", "unchecked"})
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request)
	{
		UserProfile username = userManager.getRemoteUser(request);
		if (username == null || !userManager.isSystemAdmin(username.getUserKey()))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.ok(transactionTemplate.execute(new TransactionCallback()
		{
			public Object doInTransaction()
			{
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
				Config config = new Config();
				config.setUsername((String) settings.get(Config.class.getName() + ".username"));
				config.setApikey((String) settings.get(Config.class.getName() + ".apikey"));
				config.setServicekey((String) settings.get(Config.class.getName() + ".servicekey"));
				config.setUrl((String) settings.get(Config.class.getName() + ".url"));

				return config;
			}
		})).build();
	}
	@SuppressWarnings("unchecked")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(final Config config, @Context HttpServletRequest request)
	{
		UserProfile username = userManager.getRemoteUser(request);
		if (username == null || !userManager.isSystemAdmin(username.getUserKey()))
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback()
		{
			public Object doInTransaction()
			{
				PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
				pluginSettings.put(Config.class.getName() + ".username", config.getUsername());
				pluginSettings.put(Config.class.getName()  +".apikey", config.getApikey());
				pluginSettings.put(Config.class.getName()  +".servicekey", config.getServicekey());
				pluginSettings.put(Config.class.getName()  +".url", config.getUrl());
				return null;
			}
		});
		return Response.noContent().build();
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class Config
	{
		@XmlElement private String username;
		@XmlElement private String apikey;
		@XmlElement private String servicekey;
		@XmlElement private String url;

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public String getApikey() {
			return apikey;
		}

		public void setApikey(String apikey) {
			this.apikey = apikey;
		}

		public String getServicekey() {
			return servicekey;
		}

		public void setServicekey(String servicekey) {
			this.servicekey = servicekey;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

	}

}