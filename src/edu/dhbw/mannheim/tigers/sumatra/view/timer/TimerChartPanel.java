/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.timer;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TimerInfo;


/**
 * This panels visualizes the actual timer-data
 * 
 * @author Gero
 * 
 */
public class TimerChartPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -422217644518603954L;
	

	private static final float	NS_TO_MS				= 1000000;
	
	/** Maximum time everything is allowed to take (currently {@value}, 60 FPS)*/
	private static final float MAXIMUM_DURATION	= 16.7f;
	
	private final Chart2D		chart					= new Chart2D();
	private final ITrace2D		maximum				= new Trace2DLtd(20);
	private final ITrace2D		camTrace				= new Trace2DLtd(20);
	private final ITrace2D		wpTrace				= new Trace2DLtd(20);
	private final ITrace2D		aiTrace				= new Trace2DLtd(20);
	
	private final long			timeOffset			= System.nanoTime();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TimerChartPanel()
	{
		setLayout(new MigLayout("fill"));

		chart.setBackground(this.getBackground());
		chart.setForeground(Color.BLACK);
		chart.setDoubleBuffered(true);
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 20.0)));
		chart.getAxisY().setAxisTitle(new AxisTitle("Pass [ms]"));
		chart.getAxisX().setAxisTitle(new AxisTitle("t [ms]"));
		
		maximum.setColor(Color.RED);
		maximum.setName("Maximum time");
		chart.addTrace(maximum);
		
		camTrace.setColor(Color.GREEN);
		camTrace.setName("Cam");
		chart.addTrace(camTrace);
		
		wpTrace.setColor(Color.BLUE);
		wpTrace.setName("WorldPredictor");
		chart.addTrace(wpTrace);
		
		aiTrace.setColor(Color.ORANGE);
		aiTrace.setName("AI");
		chart.addTrace(aiTrace);
		
		add(chart, "grow");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void onNewTimerInfo(TimerInfo info)
	{
		long now = System.nanoTime();
		float step = (now - timeOffset) / NS_TO_MS;
		
		maximum.addPoint(step, MAXIMUM_DURATION);
		
		float timing = info.getCamTiming().duration / NS_TO_MS;
		camTrace.addPoint(step, timing);
		
		timing += info.getWpTiming().duration / NS_TO_MS;
		wpTrace.addPoint(step, timing);
		
		timing += info.getAiTiming().duration / NS_TO_MS;
		aiTrace.addPoint(step, timing);
	}
	
	
	public void clearChart()
	{
		camTrace.removeAllPoints();
		wpTrace.removeAllPoints();
		aiTrace.removeAllPoints();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setVisible(boolean visible)
	{
		chart.setVisible(visible);
	}
}
