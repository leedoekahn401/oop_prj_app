package project.app.humanelogistics.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ChartService {

    // Palette of colors to cycle through for multiple lines
    private static final Color[] SERIES_COLORS = {
            new Color(230, 126, 34),  // Orange (Default/News)
            new Color(52, 152, 219),  // Blue (Social)
            new Color(46, 204, 113),  // Green
            new Color(155, 89, 182),  // Purple
            new Color(231, 76, 60)    // Red
    };

    public File generateAndSaveChart(String title, String xAxis, String yAxis, TimeSeriesCollection dataset, String filepath) throws IOException {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, xAxis, yAxis,
                dataset, true, true, false
        );

        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        // DYNAMICALLY STYLE ALL SERIES
        // This loop makes it easy to add more lines later without changing code here.
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            // Pick color from palette (looping if we run out)
            renderer.setSeriesPaint(i, SERIES_COLORS[i % SERIES_COLORS.length]);
            renderer.setSeriesStroke(i, new BasicStroke(2.5f));
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShapesFilled(i, true);
        }

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MMM dd"));
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, 800, 500);
        return file;
    }
}