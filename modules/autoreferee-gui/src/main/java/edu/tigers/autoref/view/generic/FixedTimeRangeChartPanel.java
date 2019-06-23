/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.generic;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.traces.painters.TracePainterLine;
import info.monitorenter.util.Range;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;


/**
 * This chart class displays time based values in a fixed time range like an Oscilloscope. To achieve this data points
 * are wrapped around if they exceed the displayed range. It uses a specialized painter class to avoid painting long
 * strokes which are caused by large differences in x.
 * 
 * @author "Lukas Magel"
 */
public class FixedTimeRangeChartPanel extends JPanel
{
	/**  */
	private static final long				serialVersionUID	= -5176647826548801416L;
	
	private long								timeRange;
	
	private Chart2D							mainChart			= new Chart2D();
	private Trace2DLtd						mainTrace			= new Trace2DLtd();
	private Map<String, Trace2DSimple>	horizontalLines	= new HashMap<>();
	
	private boolean							highlightHead		= true;
	private Trace2DLtd						headTrace			= new Trace2DLtd(1);
	
	
	/**
	 * @param timeRange The time range that will be displayed in nanoseconds
	 * @param highlightHead If true the point which was inserted last will be highlighted
	 */
	public FixedTimeRangeChartPanel(final long timeRange, final boolean highlightHead)
	{
		this.highlightHead = highlightHead;
		
		setLayout(new BorderLayout());
		add(mainChart, BorderLayout.CENTER);
		
		setupChart();
		
		setRange(timeRange);
	}
	
	
	private void setupChart()
	{
		IAxis<?> xAxis = mainChart.getAxisX();
		IAxis<?> yAxis = mainChart.getAxisY();
		
		xAxis.setPaintGrid(true);
		xAxis.setRangePolicy(new RangePolicyFixedViewport());
		yAxis.setPaintGrid(true);
		
		/*
		 * Setup the main trace of the graph
		 * The trace uses the special painter class to avoid long strokes which are caused by larger x value differences.
		 */
		mainTrace.setTracePainter(new NoCarriageReturnLinePainter());
		mainTrace.setName(null);
		mainChart.addTrace(mainTrace);
		
		headTrace.setName(null);
		headTrace.setTracePainter(new TracePainterDisc(12));
		mainChart.addTrace(headTrace);
		
		mainChart.setGridColor(Color.LIGHT_GRAY);
	}
	
	
	/**
	 * Add a new data point to the chart
	 * 
	 * @param timestamp in nanoseconds
	 * @param y
	 */
	public void addPoint(final long timestamp, final double y)
	{
		double x = (timestamp % timeRange) / 1e9;
		mainTrace.addPoint(x, y);
		
		if (highlightHead)
		{
			headTrace.addPoint(x, y);
		}
	}
	
	
	/**
	 * Set the displayed y range
	 * 
	 * @param min
	 * @param max
	 */
	public void clipY(final double min, final double max)
	{
		mainChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(min, max)));
	}
	
	
	/**
	 * Set the color of the data plot
	 * 
	 * @param color
	 */
	public void setColor(final Color color)
	{
		mainTrace.setColor(color);
		headTrace.setColor(color);
	}
	
	
	/**
	 * X axis title
	 * 
	 * @param title
	 */
	public void setXTitle(final String title)
	{
		mainChart.getAxisX().getAxisTitle().setTitle(title);
	}
	
	
	/**
	 * Y axis title
	 * 
	 * @param title
	 */
	public void setYTitle(final String title)
	{
		mainChart.getAxisY().getAxisTitle().setTitle(title);
	}
	
	
	/**
	 * The size of the data point buffer of the chart
	 * 
	 * @return
	 */
	public int getPointBufferSize()
	{
		return mainTrace.getMaxSize();
	}
	
	
	/**
	 * The time range that is displayed
	 * 
	 * @param range_ns in nanoseconds
	 */
	public void setRange(final long range_ns)
	{
		timeRange = range_ns;
		mainChart.getAxisX().setRange(new Range(0, timeRange / 1e9));
	}
	
	
	/**
	 * @param bufSize
	 */
	public void setPointBufferSize(final int bufSize)
	{
		mainTrace.setMaxSize(bufSize);
	}
	
	
	/**
	 * Whether or not the current head of the graph should be highlighted
	 * 
	 * @param val
	 */
	public void setHighlightHead(final boolean val)
	{
		highlightHead = val;
		if (!highlightHead)
		{
			headTrace.removeAllPoints();
		}
	}
	
	
	/**
	 * @param name
	 * @param color
	 * @param yValue
	 */
	public void setHorizontalLine(final String name, final Color color, final double yValue)
	{
		if (!horizontalLines.containsKey(name))
		{
			Trace2DSimple trace = new Trace2DSimple(null);
			mainChart.addTrace(trace);
			
			trace.setZIndex(mainTrace.getZIndex() - 1);
			horizontalLines.put(name, trace);
		}
		Trace2DSimple trace = horizontalLines.get(name);
		
		trace.setColor(color);
		trace.removeAllPoints();
		trace.addPoint(Double.MIN_VALUE, yValue);
		trace.addPoint(Double.MAX_VALUE, yValue);
	}
	
	
	/**
	 * Calculates the data point buffer size of the chart to accomodate enough points to fill 90 percent of its width if
	 * new data points arrive with a time delta (T) of {@code updatePeriod} nanoseconds.
	 * 
	 * @param updatePeriod in nanoseconds
	 */
	public void setPointBufferSizeWithPeriod(final long updatePeriod)
	{
		if (updatePeriod <= 0)
		{
			return;
		}
		
		int requiredSize = (int) (((timeRange * 90) / 100) / updatePeriod);
		setPointBufferSize(requiredSize);
	}
	
	
	/**
	 * Remove all currently displayed data points
	 */
	public void clear()
	{
		mainTrace.removeAllPoints();
	}
	
	/**
	 * Special line painter class which only draws a line if the x values of line start and end are not spaced too far
	 * apart.
	 * 
	 * @author "Lukas Magel"
	 */
	private class NoCarriageReturnLinePainter extends TracePainterLine
	{
		
		/**  */
		private static final long	serialVersionUID	= 672321723106037578L;
		
		
		@Override
		public void paintPoint(final int absoluteX, final int absoluteY, final int nextX, final int nextY,
				final Graphics g, final ITracePoint2D original)
		{
			if (Math.abs((nextX - absoluteX)) < 10)
			{
				super.paintPoint(absoluteX, absoluteY, nextX, nextY, g, original);
			}
		}
		
	}
	
	
}
