package com.contrastsecurity;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * Created by donaldpropst on 12/29/16.
 */
@Named("ContrastPlugin")
public class PostBuildServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @ComponentImport
    private final TemplateRenderer templateRenderer;
    @ComponentImport
    private final ActiveObjects activeObjects;

    @Inject
    public PostBuildServlet(TemplateRenderer templateRenderer, ActiveObjects activeObjects){
        this.templateRenderer = templateRenderer;
        this.activeObjects = activeObjects;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        templateRenderer.render("postBuild.vm", response.getWriter());
    }



}
