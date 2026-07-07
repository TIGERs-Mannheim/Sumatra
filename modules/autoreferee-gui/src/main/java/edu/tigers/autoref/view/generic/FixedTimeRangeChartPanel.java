package edu.tigers.autoref.view.generic;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyManualTicks;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.traces.painters.TracePainterLine;
import info.monitorenter.util.Range;

import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;


public class FixedTimeRangeChartPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -5176647826548801416L;

	private long timeRange;

	private Chart2D mainChart = new Chart2D();
	private Trace2DLtd mainTrace = new Trace2DLtd();
	private Trace2DLtd initialVelTrace = new Trace2DLtd();
	private Map<String, Trace2DSimple> horizontalLines = new HashMap<>();

	private boolean highlightHead;
	private Trace2DLtd headTrace = new Trace2DLtd(1);


	public FixedTimeRangeChartPanel(final long timeRange, final boolean highlightHead)
	{
		this.highlightHead = highlightHead;

		setLayout(new BorderLayout());
		add(mainChart, BorderLayout.CENTER);

		setupChart();

		setRange(timeRange);
	}


	private void applyTheme()
	{
		if (mainChart == null)
		{
			return;
		}

		Color bg = UIManager.getColor("Panel.background");
		Color fg = UIManager.getColor("Label.foreground");
		Color grid = fg.darker();

		setBackground(bg);

		mainChart.setBackground(bg);
		mainChart.setForeground(fg);
		mainChart.setGridColor(grid);

		mainChart.getAxisX().getAxisTitle().setTitleColor(fg);
		mainChart.getAxisY().getAxisTitle().setTitleColor(fg);
	}


	private void setupChart()
	{
		IAxis<?> xAxis = mainChart.getAxisX();
		IAxis<?> yAxis = mainChart.getAxisY();

		xAxis.setPaintGrid(true);
		xAxis.setRangePolicy(new RangePolicyFixedViewport());
		yAxis.setPaintGrid(true);

		mainTrace.setTracePainter(new NoCarriageReturnLinePainter());
		mainTrace.setName(null);
		mainChart.addTrace(mainTrace);

		initialVelTrace.setTracePainter(new NoCarriageReturnLinePainter());
		initialVelTrace.setName(null);
		initialVelTrace.setColor(Color.GREEN);
		mainChart.addTrace(initialVelTrace);

		headTrace.setName(null);
		headTrace.setTracePainter(new TracePainterDisc(12));
		mainChart.addTrace(headTrace);

		mainChart.setGridColor(Color.LIGHT_GRAY);

		applyTheme();
	}


	public void addPoint(final long timestamp, final double y)
	{
		double x = (timestamp % timeRange) / 1e9;
		mainTrace.addPoint(x, y);

		if (highlightHead)
		{
			headTrace.addPoint(x, y);
		}
	}


	public void addInitialVelPoint(final long timestamp, final double y)
	{
		double x = (timestamp % timeRange) / 1e9;
		initialVelTrace.addPoint(x, y);
	}


	public void clipY(final double min, final double max)
	{
		mainChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(min, max)));
	}


	@SuppressWarnings("unchecked")
	public void yTicks(double spacing)
	{
		AAxis<IAxisScalePolicy> yAxis = (AAxis<IAxisScalePolicy>) mainChart.getAxisY();
		yAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks());
		yAxis.setMinorTickSpacing(spacing);
	}


	public void setColor(final Color color)
	{
		mainTrace.setColor(color);
		headTrace.setColor(color);
	}


	public void setXTitle(final String title)
	{
		mainChart.getAxisX().getAxisTitle().setTitle(title);
	}


	public void setYTitle(final String title)
	{
		mainChart.getAxisY().getAxisTitle().setTitle(title);
	}


	public int getPointBufferSize()
	{
		return mainTrace.getMaxSize();
	}


	public void setRange(final long range_ns)
	{
		timeRange = range_ns;
		mainChart.getAxisX().setRange(new Range(0, timeRange / 1e9));
	}


	public void setPointBufferSize(final int bufSize)
	{
		mainTrace.setMaxSize(bufSize);
		initialVelTrace.setMaxSize(bufSize);
	}


	public void setHighlightHead(final boolean val)
	{
		highlightHead = val;
		if (!highlightHead)
		{
			headTrace.removeAllPoints();
		}
	}


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


	public void setPointBufferSizeWithPeriod(final long updatePeriod)
	{
		if (updatePeriod <= 0)
		{
			return;
		}

		int requiredSize = (int) (((timeRange * 90) / 100) / updatePeriod);
		setPointBufferSize(requiredSize);
	}


	public void clear()
	{
		mainTrace.removeAllPoints();
	}


	private static class NoCarriageReturnLinePainter extends TracePainterLine
	{
		@Serial
		private static final long serialVersionUID = 672321723106037578L;


		@Override
		public void paintPoint(
				final int absoluteX, final int absoluteY, final int nextX, final int nextY,
				final Graphics g, final ITracePoint2D original
		)
		{
			if (Math.abs((nextX - absoluteX)) < 10)
			{
				super.paintPoint(absoluteX, absoluteY, nextX, nextY, g, original);
			}
		}
	}


	@Override
	public void updateUI()
	{
		super.updateUI();
		applyTheme();
	}
}