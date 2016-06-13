package com.contrastsecurity;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.Maps;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@Named("myPlugin")
public class ConnectionServlet extends HttpServlet
{
	private static final String PLUGIN_STORAGE_KEY = "com.contrastsecurity";
	@ComponentImport
	private final UserManager userManager;
	@ComponentImport
	private final LoginUriProvider loginUriProvider;
	@ComponentImport
	private final TemplateRenderer templateRenderer;
	@ComponentImport
	private final PluginSettingsFactory pluginSettingsFactory;

	@Inject
	public ConnectionServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, PluginSettingsFactory pluginSettingsFactory) {
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.templateRenderer = templateRenderer;
		this.pluginSettingsFactory = pluginSettingsFactory;
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username))
		{
			redirectToLogin(request, response);
			return;
		}
		Map<String, Object> context = Maps.newHashMap();

		PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

		if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".username") == null){
			String noName = "Enter a username.";
			pluginSettings.put(PLUGIN_STORAGE_KEY +".username", noName);
		}
		if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".apikey") == null){
			String noAKey = "Enter an API key.";
			pluginSettings.put(PLUGIN_STORAGE_KEY + ".apikey", noAKey);
		}
		if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".servicekey") == null){
			String noSKey = "Enter an Service key.";
			pluginSettings.put(PLUGIN_STORAGE_KEY + ".servicekey", noSKey);
		}
		if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".url") == null){
			String noURL = "Enter an Teamserver Url key.";
			pluginSettings.put(PLUGIN_STORAGE_KEY + ".url", noURL);
		}

		context.put("username", pluginSettings.get(PLUGIN_STORAGE_KEY + ".username"));
		context.put("apikey", pluginSettings.get(PLUGIN_STORAGE_KEY + ".apikey"));
		context.put("servicekey", pluginSettings.get(PLUGIN_STORAGE_KEY + ".servicekey"));
		context.put("url", pluginSettings.get(PLUGIN_STORAGE_KEY + ".url"));
		
		response.setContentType("text/html;charset=utf-8");
		templateRenderer.render("admin.vm", response.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
		throws ServletException, IOException {
	PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
	pluginSettings.put(PLUGIN_STORAGE_KEY + ".username", req.getParameter("username"));
	pluginSettings.put(PLUGIN_STORAGE_KEY + ".apikey", req.getParameter("apiKey"));
	pluginSettings.put(PLUGIN_STORAGE_KEY + ".servicekey", req.getParameter("serviceKey"));
	pluginSettings.put(PLUGIN_STORAGE_KEY + ".url", req.getParameter("url"));
	
	response.sendRedirect("teamserverConnect");
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
	}
	private URI getUri(HttpServletRequest request)
	{
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null)
		{
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}

	// This is what your MyPluginServlet.java should look like in its final stages.

}