package com.contrastsecurity;

import com.atlassian.bamboo.bandana.BambooBandanaManager;
import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.reports.collector.ReportCollector;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.bandana.DefaultBandanaManager;
import com.atlassian.bandana.impl.MemoryBandanaPersister;
import org.jetbrains.annotations.NotNull;
import org.jfree.data.general.Dataset;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class SeverityReportCollector implements ReportCollector {

    @NotNull
    public Dataset getDataset() {
        return null;
    }

    public void setResultsList(@NotNull List<? extends ResultsSummary> list) {


        //System.out.println(m.getValue(, "com.contrastsecurity.bambooplugin:A"));
        System.out.println("******************************************************************************************************************************************************************");
        System.out.println("set result list");
        for(ResultsSummary l : list){
            PlanAwareBandanaContext.forPlan(l.getImmutablePlan());
            System.out.println(l.getBuildKey());
            System.out.println(l.getBuildResultKey());
            System.out.println(l.getPlanResultKey().getKey());
            System.out.println(l.getPlanResultKey().getPlanKey());
            System.out.println(l.getPlanResultKey().getBuildNumber());
            System.out.println(l.getPlanResultKey().getEntityKey());

            System.out.println(l.getImmutablePlan().getKey());
            System.out.println(l.getImmutablePlan().getBuildKey());
            System.out.println(l.getImmutablePlan().getPlanKey().getKey());
            System.out.println(l.getPlanKey().getKey());

            //System.out.println(l.get);
        }
    }

    public void setParams(@NotNull Map<String, String[]> map) {

    }

    public String getPeriodRange() {
        return null;
    }
}