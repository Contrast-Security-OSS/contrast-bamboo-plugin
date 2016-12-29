package com.contrastsecurity;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

public class ContrastSeverityReportChart {

    private JFreeChart chart;
    public ContrastSeverityReportChart(){
        chart = new JFreeChart("Test", new XYPlot());
        System.out.println("CHART CONSTRUCTOR");
    }
}
