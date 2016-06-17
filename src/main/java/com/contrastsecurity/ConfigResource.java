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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	public static final class Config
	{
		@JsonProperty("username") 
		public String username;
		@JsonProperty("apikey")
		public String apikey;
		@JsonProperty("servicekey")
		public String servicekey;
		@JsonProperty("url") 
		public String url;

		public Config(String username, String apikey, String servicekey, String url){
			this.username = username;
			this.apikey = apikey;
			this.servicekey = servicekey;
			this.url = url;
		}
		public Config(){};

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
				//PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
				Config config = new Config();
				/*config.setUsername((String) settings.get(PLUGIN_STORAGE_KEY + ".username"));				
				config.setApikey((String) settings.get(PLUGIN_STORAGE_KEY + ".apikey"));
				config.setServicekey((String) settings.get(PLUGIN_STORAGE_KEY + ".servicekey"));
				config.setUrl((String) settings.get(PLUGIN_STORAGE_KEY + ".url"));
				 */
				config.setUsername("configUsername");	
				config.setApikey("configApi");
				config.setServicekey("configServ");
				config.setUrl("configURL");
				return config;
			}
		})).build();
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(final Config config, @Context HttpServletRequest request)
	{
		System.out.println(request.toString());
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