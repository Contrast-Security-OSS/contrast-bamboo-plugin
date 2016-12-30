package com.contrastsecurity;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.gson.Gson;
import net.java.ao.Query;
import org.apache.maven.model.Build;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    @ComponentImport
    private final ResultsSummaryManager summary;

    @Inject
    public PostBuildServlet(TemplateRenderer templateRenderer, ActiveObjects activeObjects, ResultsSummaryManager summaryManager){
        this.templateRenderer = templateRenderer;
        this.activeObjects = activeObjects;
        this.summary = summaryManager;

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

            Map<String, Object> map = new HashMap<String, Object>();


            map.put("results", getPreviousBuildResults());

            response.setContentType("text/html;charset=utf-8");
            templateRenderer.render("postBuild.vm", map,response.getWriter());


    }

    public ArrayList<BuildResults> getPreviousBuildResults(){
        ArrayList<BuildResults> results = new ArrayList<BuildResults>();
        HashMap<String, BuildResults> resultMap = new HashMap<String, BuildResults>();
        String key = VerifyThresholdsTask.DATA_STORAGE_CONTRAST + summary.getLatestResultSummaries().get(0).getImmutablePlan().getKey();
        for(Finding f : activeObjects.find(Finding.class, Query.select().where("BUILD_ID LIKE ?", key+"%").order("ID DESC"))){
            if(f.getBuildId() != null){
                if(!resultMap.containsKey(f.getBuildId())){
                    resultMap.put(f.getBuildId(), new BuildResults(f.getBuildId(), f));
                }else{
                    resultMap.get(f.getBuildId()).addFinding(f);
                }
            }

        }
        for(String s : resultMap.keySet()){
            results.add(resultMap.get(s));
        }
        return results;
    }



}
