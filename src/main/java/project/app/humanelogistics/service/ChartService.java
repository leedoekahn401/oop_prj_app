package project.app.humanelogistics.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ChartService {

    public File generateAndSaveChart(String title, String xAxis, String yAxis, TimeSeriesCollection dataset, String filepath) throws IOException {
        // 1. Create the base chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, xAxis, yAxis,
                dataset, true, true, false
        );

        // 2. Global Style (Remove ugly gray background)
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart.getTitle().setPaint(new Color(44, 62, 80)); // Dark Slate (#2C3E50)

        // 3. Plot Area Style
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(236, 240, 241)); // Very light gray
        plot.setRangeGridlinePaint(new Color(236, 240, 241));
        plot.setOutlineVisible(false); // Remove border around the graph
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

        // 4. Line Style (Renderer)
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        // Series 0: The Sentiment Line
        renderer.setSeriesPaint(0, new Color(230, 126, 34)); // Orange (#E67E22) to match App Theme
        renderer.setSeriesStroke(0, new BasicStroke(2.5f)); // Thicker line (2.5px)

        // Add dots/markers at data points
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesFilled(0, true);

        // 5. Axes Style
        // X-Axis (Date)
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MMM dd")); // e.g. "Sep 08"
        domainAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setTickLabelPaint(Color.GRAY);

        // Y-Axis (Score)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(Color.GRAY);

        // 6. Save the image
        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, 800, 500); // Increased resolution slightly
        return file;
    }
}