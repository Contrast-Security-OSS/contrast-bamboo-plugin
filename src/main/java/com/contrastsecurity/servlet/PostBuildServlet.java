package com.contrastsecurity.servlet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.contrastsecurity.task.VerifyThresholdsTask;
import com.contrastsecurity.model.BuildResults;
import com.contrastsecurity.model.Finding;
import com.contrastsecurity.util.KeyGenerator;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("ContrastPlugin")
public class PostBuildServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @ComponentImport
    private final TemplateRenderer templateRenderer;
    @ComponentImport
    private final ActiveObjects activeObjects;

    private String buildKey;


    @Inject
    public PostBuildServlet(TemplateRenderer templateRenderer, ActiveObjects activeObjects){
        this.templateRenderer = templateRenderer;
        this.activeObjects = activeObjects;

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            buildKey = request.getParameter("buildKey");

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("results", getPreviousBuildResults(buildKey));

            response.setContentType("text/html;charset=utf-8");
            response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            templateRenderer.render("postBuild.vm", map,response.getWriter());

    }

    public ArrayList<BuildResults> getPreviousBuildResults(String parameterKey){
        ArrayList<BuildResults> results = new ArrayList<BuildResults>();
        HashMap<String, BuildResults> resultMap = new HashMap<String, BuildResults>();
        String key = VerifyThresholdsTask.DATA_STORAGE_CONTRAST + KeyGenerator.generate(parameterKey);
        Finding[] findings = retrieveFindings(key);
        for(Finding f : findings){
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

        sort(results);
        return new ArrayList<BuildResults>(limit(results));
    }

    private Finding[] retrieveFindings(String key){
        Finding[] findings = activeObjects.find(Finding.class, Query.select()
                .order("ID DESC")
                .where("BUILD_ID LIKE ?", key+"%"));

        return findings;
    }
    public List<BuildResults> limit(List<BuildResults> results){
        if(results.size() > 10)
            return results.subList(0,10);
        return results;
    }
    public void sort(ArrayList<BuildResults> results){
        if(results == null) return;
        Collections.sort(results, new Comparator<BuildResults>(){
            public int compare(BuildResults o1, BuildResults o2){
                if(Integer.parseInt(o1.getBuildId()) == Integer.parseInt(o2.getBuildId()))
                    return 0;
                return Integer.parseInt(o1.getBuildId()) > Integer.parseInt(o2.getBuildId()) ? -1 : 1;
            }
        });

    }



}
