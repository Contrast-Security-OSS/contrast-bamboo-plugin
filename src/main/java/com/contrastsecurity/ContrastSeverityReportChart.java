package com.contrastsecurity;

import com.atlassian.bamboo.charts.AbstractBambooChart;
import com.atlassian.bamboo.charts.ChartBuilder;
import com.atlassian.bamboo.charts.utils.ChartDefaults;
import com.atlassian.bamboo.charts.utils.ChartUtil;
import com.atlassian.bamboo.reports.charts.BambooReportLineChart;
import com.atlassian.bamboo.reports.charts.BuildSummarySuccessRatioLineChart;
import com.atlassian.bamboo.util.NumberUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import java.util.Map;

public class ContrastSeverityReportChart extends BambooReportLineChart {

    public ContrastSeverityReportChart() {
        System.out.println("DONNIE CONSTRUCTOR ***************************** ");
        this.setyAxisLabel("% Build Successful");
        getJFreeChart();
    }

    public JFreeChart getJFreeChart() {
        System.out.println("DONNIE GET JFREECHART ***************************** ");
        JFreeChart jFreeChart1 = super.getJFreeChart();
        XYPlot xyPlot = jFreeChart1.getXYPlot();
        xyPlot.getRangeAxis().setUpperBound(100.0D);

            XYAreaRenderer renderer = new XYAreaRenderer(5);
            ChartUtil.populateRendererDefaults(this, this, renderer);
            renderer.setOutline(true);
            renderer.setSeriesOutlinePaint(0, ChartDefaults.BRIGHT_GREEN);
            renderer.setSeriesPaint(0, ChartDefaults.GREEN_DIFF);
            xyPlot.setRenderer(renderer);


        return jFreeChart1;
    }

}
