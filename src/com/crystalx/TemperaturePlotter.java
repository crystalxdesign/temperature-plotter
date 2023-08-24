package com.crystalx;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYDataset;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

public class TemperaturePlotter  extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TOOLTITLE = "Temperature Plotter V0.2";
	private static JFreeChart chart;
	private static XYItemRenderer theRenderer;
	private static NumberAxis range;
	
	private JPanel createChartPanel() {
	    String xAxisLabel = "Time (s)";
	    String yAxisLabel = "Temperature (C)";
	    XYDataset dataset = ControlPanel.getData();
	 
	    chart = ChartFactory.createXYLineChart(TOOLTITLE, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, false, false, false);
	    XYPlot plot = (XYPlot)chart.getPlot();
	    theRenderer = plot.getRenderer(0);
	    range = (NumberAxis) plot.getRangeAxis();
	    return new ChartPanel(chart);
	}
	
    public static void setRangeManual(double lower, double upper) {
    	range.setRange(lower, upper);
    }

    public static void setRangeAuto() {
    	range.setRange(range.getLowerBound(), range.getUpperBound());
    }

    public static void setSeriesVisibility(int series, boolean visible) {
	    theRenderer.setSeriesVisible(series, visible);
    }

    public static void setGraphTitle(String title) {
    	chart.setTitle(title);
    }

    public static String getGraphTitle() {
    	return chart.getTitle().getText();
    }

    public TemperaturePlotter() {
        super(TOOLTITLE);
 
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
        JPanel controlPanel = ControlPanel.build();
        add(controlPanel, BorderLayout.EAST);

        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TemperaturePlotter().setVisible(true);
            }
        });
    }
}
