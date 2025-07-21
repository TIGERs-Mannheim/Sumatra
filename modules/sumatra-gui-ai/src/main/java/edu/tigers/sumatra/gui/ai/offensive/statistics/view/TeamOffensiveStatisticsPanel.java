/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.offensive.statistics.view;

import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.ai.metis.pass.PassStatsKickReceiveVelocity;
import lombok.Setter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class VariableSizeScatterRenderer extends XYLineAndShapeRenderer
{
	private final List<List<Double>> seriesDurations;
	private final double minDuration;
	private final double minSize;
	private final double maxSize;
	private final double durationRange;


	public VariableSizeScatterRenderer(List<List<Double>> seriesDurations,
			double minDuration, double maxDuration,
			double minSize, double maxSize)
	{
		super(false, true);
		this.seriesDurations = seriesDurations;
		this.minDuration = minDuration;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.durationRange = maxDuration - minDuration;
	}


	@Override
	public Shape getItemShape(int series, int item)
	{
		if (seriesDurations == null || series >= seriesDurations.size() ||
				seriesDurations.get(series) == null || item >= seriesDurations.get(series).size())
		{
			return super.getItemShape(series, item);
		}

		double duration = seriesDurations.get(series).get(item);
		double normalizedDuration = (duration - minDuration) / durationRange;
		double diameter = minSize + (maxSize - minSize) * normalizedDuration;
		diameter = Math.max(minSize, Math.min(maxSize, diameter));
		double radius = diameter / 2.0;
		return new Ellipse2D.Double(-radius, -radius, diameter, diameter);
	}


	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof VariableSizeScatterRenderer that))
			return false;
		if (!super.equals(o))
			return false;
		return Double.compare(minDuration, that.minDuration) == 0
				&& Double.compare(minSize, that.minSize) == 0 && Double.compare(maxSize, that.maxSize) == 0
				&& Double.compare(durationRange, that.durationRange) == 0 && Objects.equals(
				seriesDurations, that.seriesDurations);
	}


	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), seriesDurations, minDuration, minSize, maxSize, durationRange);
	}
}


public class TeamOffensiveStatisticsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 5289153581163080891L;

	private transient PassStats passStats;
	private final ChartPanel chartPanel;

	private static final Color PLANNED_SUCCESS_COLOR = Color.GREEN.darker();
	private static final Color ACTUAL_SUCCESS_COLOR = Color.BLUE;
	private static final Color LINE_SUCCESS_COLOR = Color.CYAN.darker();

	private static final Color PLANNED_FAILURE_COLOR = Color.ORANGE;
	private static final Color ACTUAL_FAILURE_COLOR = Color.RED;
	private static final Color LINE_FAILURE_COLOR = Color.GRAY;

	private static final double MIN_MARKER_DIAMETER = 5.0;
	private static final double MAX_MARKER_DIAMETER = 15.0;

	@Setter
	private boolean filterRegular;
	@Setter
	private boolean filterRedirects;


	public TeamOffensiveStatisticsPanel()
	{
		setLayout(new BorderLayout());
		JFreeChart chart = createEmptyChart();
		chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
	}


	public void updatePassStats(PassStats passStats)
	{
		this.passStats = passStats;
		updateChart();
	}


	private JFreeChart createEmptyChart()
	{
		XYSeriesCollection dataset = new XYSeriesCollection();
		JFreeChart chart = ChartFactory.createScatterPlot(
				"Pass Stats",
				"Angle (degrees)",
				"Velocity (m/s)",
				dataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		return chart;
	}


	private void updateChart()
	{
		if (passStats == null || passStats.getReceiveVelocities() == null || passStats.getReceiveVelocities().isEmpty())
		{
			chartPanel.setChart(createEmptyChart());
			return;
		}

		Stream<PassStatsKickReceiveVelocity> velocities = passStats.getReceiveVelocities().stream();

		if (filterRegular)
		{
			velocities = velocities.filter(v -> !v.redirect());
		}

		if (filterRedirects)
		{
			velocities = velocities.filter(PassStatsKickReceiveVelocity::redirect);
		}

		XYSeries plannedSuccessSeries = new XYSeries("Planned (Success)");
		XYSeries actualSuccessSeries = new XYSeries("Actual (Success)");
		XYSeries plannedFailureSeries = new XYSeries("Planned (Failure)");
		XYSeries actualFailureSeries = new XYSeries("Actual (Failure)");

		List<Double> plannedSuccessDurations = new ArrayList<>();
		List<Double> actualSuccessDurations = new ArrayList<>();
		List<Double> plannedFailureDurations = new ArrayList<>();
		List<Double> actualFailureDurations = new ArrayList<>();

		XYSeriesCollection scatterDataset = new XYSeriesCollection();
		scatterDataset.addSeries(plannedSuccessSeries);
		scatterDataset.addSeries(plannedFailureSeries);
		scatterDataset.addSeries(actualSuccessSeries);
		scatterDataset.addSeries(actualFailureSeries);

		XYSeriesCollection successLineDataset = new XYSeriesCollection();
		XYSeriesCollection failureLineDataset = new XYSeriesCollection();

		int successLineIndex = 0;
		int failureLineIndex = 0;
		for (PassStatsKickReceiveVelocity vel : velocities.collect(Collectors.toSet()))
		{
			double plannedAngle = vel.planned().getAngle();
			double plannedVel = vel.planned().getLength();
			double actualAngle = vel.actual().getAngle();
			double actualVel = vel.actual().getLength();
			double durationActual = vel.passDurationActual();
			double durationPlanned = vel.passDurationPlanned();

			if (vel.success())
			{
				plannedSuccessSeries.add(plannedAngle, plannedVel);
				plannedSuccessDurations.add(durationActual);
				actualSuccessSeries.add(actualAngle, actualVel);
				actualSuccessDurations.add(durationPlanned);

				XYSeries line = new XYSeries("SuccessLine" + successLineIndex++);
				line.add(plannedAngle, plannedVel);
				line.add(actualAngle, actualVel);
				successLineDataset.addSeries(line);
			} else
			{
				plannedFailureSeries.add(plannedAngle, plannedVel);
				plannedFailureDurations.add(durationActual);
				actualFailureSeries.add(actualAngle, actualVel);
				actualFailureDurations.add(durationPlanned);

				XYSeries line = new XYSeries("FailureLine" + failureLineIndex++);
				line.add(plannedAngle, plannedVel);
				line.add(actualAngle, actualVel);
				failureLineDataset.addSeries(line);
			}
		}

		List<List<Double>> allScatterDurations = List.of(
				plannedSuccessDurations,
				plannedFailureDurations,
				actualSuccessDurations,
				actualFailureDurations
		);

		JFreeChart chart = ChartFactory.createScatterPlot(
				"Pass Stats",
				"Angle (degrees)",
				"Velocity (m/s)",
				scatterDataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
		);

		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		VariableSizeScatterRenderer scatterRenderer = new VariableSizeScatterRenderer(
				allScatterDurations,
				1,
				2.5,
				MIN_MARKER_DIAMETER,
				MAX_MARKER_DIAMETER
		);
		plot.setRenderer(0, scatterRenderer);

		scatterRenderer.setSeriesPaint(0, PLANNED_SUCCESS_COLOR);
		scatterRenderer.setSeriesPaint(1, PLANNED_FAILURE_COLOR);
		scatterRenderer.setSeriesPaint(2, ACTUAL_SUCCESS_COLOR);
		scatterRenderer.setSeriesPaint(3, ACTUAL_FAILURE_COLOR);

		plot.setDataset(1, successLineDataset);
		XYLineAndShapeRenderer successLineRenderer = new XYLineAndShapeRenderer(true, false);
		for (int i = 0; i < successLineDataset.getSeriesCount(); i++)
		{
			successLineRenderer.setSeriesPaint(i, LINE_SUCCESS_COLOR);
			successLineRenderer.setSeriesStroke(i, new BasicStroke(1.5f));
			successLineRenderer.setSeriesVisibleInLegend(i, false);
		}
		plot.setRenderer(1, successLineRenderer);

		plot.setDataset(2, failureLineDataset);
		XYLineAndShapeRenderer failureLineRenderer = new XYLineAndShapeRenderer(true, false);
		for (int i = 0; i < failureLineDataset.getSeriesCount(); i++)
		{
			failureLineRenderer.setSeriesPaint(i, LINE_FAILURE_COLOR);
			failureLineRenderer.setSeriesStroke(i, new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f, 3.0f }, 0.0f));
			failureLineRenderer.setSeriesVisibleInLegend(i, false);
		}
		plot.setRenderer(2, failureLineRenderer);

		LegendItemCollection legendItems = new LegendItemCollection();
		var legend1 = scatterRenderer.getLegendItem(0, 0);
		var legend2 = scatterRenderer.getLegendItem(0, 1);
		var legend3 = scatterRenderer.getLegendItem(0, 2);
		var legend4 = scatterRenderer.getLegendItem(0, 3);

		if (legend1 != null)
		{
			legendItems.add(legend1);
		}
		if (legend2 != null)
		{
			legendItems.add(legend2);
		}
		if (legend3 != null)
		{
			legendItems.add(legend3);
		}
		if (legend4 != null)
		{
			legendItems.add(legend4);
		}
		plot.setFixedLegendItems(legendItems);

		chartPanel.setChart(chart);
	}
}