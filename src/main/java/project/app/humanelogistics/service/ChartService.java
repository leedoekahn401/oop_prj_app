package project.app.humanelogistics.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ChartService {

    // Palette màu hiện đại hơn
    private static final Color[] SERIES_COLORS = {
            new Color(230, 126, 34),  // Orange
            new Color(52, 152, 219),  // Blue
            new Color(46, 204, 113),  // Green
            new Color(155, 89, 182),  // Purple
            new Color(231, 76, 60)    // Red
    };

    public File generateAndSaveChart(String title, String xAxis, String yAxis, TimeSeriesCollection dataset, String filepath) throws IOException {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, xAxis, yAxis,
                dataset, true, true, false
        );

        // 1. BẬT KHỬ RĂNG CƯA (ANTI-ALIASING) -> Giúp nét vẽ mượt mà
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        // 2. CẤU HÌNH GIAO DIỆN (Trắng sạch)
        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220)); // Grid nhạt hơn
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false); // Bỏ viền đen bao quanh biểu đồ

        // 3. CẤU HÌNH PHÔNG CHỮ (To hơn để phù hợp độ phân giải cao)
        Font titleFont = new Font("Segoe UI", Font.BOLD, 26);
        Font axisLabelFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font tickLabelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font legendFont = new Font("Segoe UI", Font.PLAIN, 16);

        // Set Font cho Title
        TextTitle chartTitle = chart.getTitle();
        chartTitle.setFont(titleFont);
        chartTitle.setPaint(new Color(44, 62, 80)); // Màu xanh đen đậm

        // Set Font cho Axis
        plot.getDomainAxis().setLabelFont(axisLabelFont);
        plot.getDomainAxis().setTickLabelFont(tickLabelFont);
        plot.getRangeAxis().setLabelFont(axisLabelFont);
        plot.getRangeAxis().setTickLabelFont(tickLabelFont);

        // Set Font cho Legend (Chú thích)
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(legendFont);
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE); // Bỏ khung viền legend
        }

        // 4. LÀM DÀY ĐƯỜNG VẼ (Stroke)
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, SERIES_COLORS[i % SERIES_COLORS.length]);
            // Tăng độ dày lên 4.0f (vì ảnh sẽ to gấp đôi)
            renderer.setSeriesStroke(i, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Hiện điểm dữ liệu (dot) nhưng nhỏ gọn hơn
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShapesFilled(i, true);
        }

        // Format ngày tháng trục hoành
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM"));
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));

        // 5. LƯU ẢNH ĐỘ PHÂN GIẢI CAO (1600x1000)
        // Khi hiển thị trên ImageView rộng 800px, ảnh sẽ bị nén lại -> Siêu nét
        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, 1600, 1000);
        return file;
    }
}