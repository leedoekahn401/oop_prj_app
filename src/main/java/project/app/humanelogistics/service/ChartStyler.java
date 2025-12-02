package project.app.humanelogistics.service;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import java.awt.*;

/**
 * Helper class to satisfy Law of Demeter.
 * Instead of chaining calls deep into the JFreeChart library in the service,
 * we encapsulate the styling logic here.
 */
public class ChartStyler {
    private final XYPlot plot;

    public ChartStyler(XYPlot plot) {
        this.plot = plot;
    }

    public ChartStyler setBackground(Color color) {
        plot.setBackgroundPaint(color);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        return this;
    }

    public ChartStyler styleSeries(int index, Color color, float strokeWidth) {
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(index, color);
        renderer.setSeriesStroke(index, new BasicStroke(strokeWidth));
        renderer.setSeriesShapesVisible(index, true);
        renderer.setSeriesShapesFilled(index, true);
        return this;
    }
}