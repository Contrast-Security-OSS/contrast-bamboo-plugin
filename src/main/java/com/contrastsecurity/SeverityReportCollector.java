package com.contrastsecurity;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.bandana.BambooBandanaManager;
import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.reports.collector.ReportCollector;
import com.atlassian.bamboo.resultsummary.ResultsSummary;

import org.jetbrains.annotations.NotNull;
import org.jfree.data.general.Dataset;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.FilterForm;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.UrlBuilder;
import com.contrastsecurity.models.*;
import com.contrastsecurity.sdk.ContrastSDK;
import com.opensymphony.xwork2.inject.Inject;

public class SeverityReportCollector implements ReportCollector {

    @ComponentImport
    private final ActiveObjects activeObjects;

    private HashMap<String, ArrayList<Finding>> buildFindings;
    @Inject
    public SeverityReportCollector(ActiveObjects activeObjects){
        this.activeObjects = activeObjects;
        buildFindings = new HashMap<String, ArrayList<Finding>>();
    }

    @NotNull
    public Dataset getDataset() {
        return null;
    }

    public void setResultsList(@NotNull List<? extends ResultsSummary> list) {
        for(ResultsSummary l : list){
            PlanAwareBandanaContext.forPlan(l.getImmutablePlan());
            final String key = l.getPlanResultKey().getKey();
            activeObjects.executeInTransaction(new TransactionCallback<Finding>(){
                public Finding doInTransaction(){
                    for(Finding f : activeObjects.find(Finding.class)){
                        if(buildFindings.get(key) == null) {
                            buildFindings.put(key, new ArrayList<Finding>());
                            buildFindings.get(key).add(f);
                        }else{
                            buildFindings.get(key).add(f);
                        }
                    }
                    return null;
                }

            });
            //System.out.println(l.get);
        }


    }

    public void setParams(@NotNull Map<String, String[]> map) {

    }

    public String getPeriodRange() {
        return null;
    }
}