package edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals;

/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.10.2010
 * Author(s): Marcel Sauer
 * 
 * *********************************************************
 */

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;


/**
 * Abstract panel to plot data on Y to moving time on X
 * 
 * @author Marcel
 * 
 */
public class Graph extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -42221764184312894L;
	
	
	private static final float	NS_TO_MS				= 1000000;
	
	
	private final Chart2D		chart					= new Chart2D();
	/*
	 * private final ITrace2D maximum = new Trace2DLtd(20);
	 * private final ITrace2D camTrace = new Trace2DLtd(20);
	 * private final ITrace2D wpTrace = new Trace2DLtd(20);
	 * private final ITrace2D aiTrace = new Trace2DLtd(20);
	 */
	
	private final long			timeOffset			= System.nanoTime();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	protected Graph(float yMin, float yMax, String yTitle)
	{
		setLayout(new MigLayout("fill, insets 0"));
		
		// chart.setBackground(this.getBackground());
		chart.setForeground(Color.BLACK);
		chart.setBackground(new Color(0.92f, 0.92f, 0.92f));
		// chart.setBackground(Color.green);
		chart.setDoubleBuffered(true);
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(yMin, yMax)));
		chart.getAxisY().setAxisTitle(new AxisTitle(yTitle));
		chart.getAxisX().setAxisTitle(new AxisTitle("t [ms]"));
		
		/*
		 * maximum.setColor(Color.RED);
		 * maximum.setName("Maximum time");
		 * chart.addTrace(maximum);
		 * 
		 * camTrace.setColor(Color.GREEN);
		 * camTrace.setName("Cam");
		 * chart.addTrace(camTrace);
		 * 
		 * wpTrace.setColor(Color.BLUE);
		 * wpTrace.setName("WorldPredictor");
		 * chart.addTrace(wpTrace);
		 * 
		 * aiTrace.setColor(Color.ORANGE);
		 * aiTrace.setName("AI");
		 * chart.addTrace(aiTrace);
		 */
		add(chart, "grow");
	}
	
	
	protected ITrace2D addTrace(Color color, String name)
	{
		ITrace2D ret = new Trace2DLtd(50);
		ret.setName(name);
		ret.setColor(color);
		chart.addTrace(ret);
		return ret;
	}
	
	
	protected void updateTrace(ITrace2D trace, long time, float y)
	{
		float step = (time - timeOffset) / NS_TO_MS;
		trace.addPoint(step, y);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/*
	 * public void onNewTimerInfo(TimerInfo info)
	 * {
	 * long now = System.nanoTime();
	 * float step = (now - timeOffset) / NS_TO_MS;
	 * 
	 * maximum.addPoint(step, MAXIMUM_DURATION);
	 * 
	 * float timing = info.camTiming.duration / NS_TO_MS;
	 * // System.out.println("Cam: " + timing);
	 * camTrace.addPoint(step, timing);
	 * 
	 * timing += info.wpTiming.duration / NS_TO_MS;
	 * // System.out.println("WP: " + timing);
	 * wpTrace.addPoint(step, timing);
	 * 
	 * timing += info.aiTiming.duration / NS_TO_MS;
	 * // System.out.println("AI: " + timing);
	 * aiTrace.addPoint(step, timing);
	 * }
	 */
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		chart.setVisible(visible);
	}
}
