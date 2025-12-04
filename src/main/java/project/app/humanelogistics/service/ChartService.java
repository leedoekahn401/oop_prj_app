package project.app.humanelogistics.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeriesCollection;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

public class ChartService {

    private static final Color[] SERIES_COLORS = {
            new Color(230, 126, 34), new Color(52, 152, 219),
            new Color(46, 204, 113), new Color(155, 89, 182), new Color(231, 76, 60)
    };

    public File generateAndSaveChart(String title, String xAxis, String yAxis, TimeSeriesCollection dataset, String filepath) throws IOException {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xAxis, yAxis, dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        ChartStyler styler = new ChartStyler(plot).setBackground(Color.WHITE);

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            styler.styleSeries(i, SERIES_COLORS[i % SERIES_COLORS.length], 2.5f);
        }

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, 800, 500);
        return file;
    }

    public File generateBarChart(String title, String xAxis, String yAxis, DefaultCategoryDataset dataset, String filepath) throws IOException {
        JFreeChart chart = ChartFactory.createBarChart(title, xAxis, yAxis, dataset);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);

        // Simple bar styling
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(52, 152, 219));

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, 800, 500);
        return file;
    }

    // THIS METHOD WAS LIKELY MISSING/EMPTY IN YOUR LOCAL FILE
    public File generatePieChart(String title, DefaultCategoryDataset dataset, String filepath) throws IOException {
        // 1. Convert Category Data to Pie Data
        DefaultPieDataset pieData = new DefaultPieDataset();
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            Comparable key = dataset.getColumnKey(i);
            Number value = dataset.getValue(0, i);
            pieData.setValue(key, value);
        }

        // 2. Create Chart
        JFreeChart chart = ChartFactory.createPieChart(title, pieData, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);

        // 3. Style Plot
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        // 4. Configure Labels (e.g. "Housing Damage")
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, 800, 500);
        return file;
    }
}