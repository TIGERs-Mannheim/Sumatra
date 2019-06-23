/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.09.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.awt.Color;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Kicker plot.
 * 
 * @author AndreR
 * 
 */
public class KickerPlotPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -2237149164069800940L;
	
	private Chart2D chart = new Chart2D();
	private ITrace2D capTrace = new Trace2DLtd(200);
	private ITrace2D chgTrace = new Trace2DLtd(200);
	
	private long timeOffset = 0;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KickerPlotPanel()
	{
		setLayout(new MigLayout("fill"));
		
		capTrace.setColor(Color.RED);
		capTrace.setName("Capacitor Level");
		chgTrace.setColor(Color.BLUE);
		chgTrace.setName("Charge Current");
		
		AAxis currentAxis = new AxisLinear();
		currentAxis.setRange(new Range(0, 10.0));
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 400.0)));
		chart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		chart.getAxisX().setMajorTickSpacing(10);
		chart.getAxisX().setMinorTickSpacing(10);
		chart.addAxisYRight(currentAxis);
		currentAxis.setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 10.0)));
		chart.setBackground(this.getBackground());
		chart.setForeground(Color.BLACK);
		chart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		chart.getAxisY().setAxisTitle(new AxisTitle("U [V]"));
		
		chart.addTrace(capTrace);
		chart.addTrace(chgTrace, chart.getAxisX(), currentAxis);

		add(chart, "grow");
		
		timeOffset = System.nanoTime();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addCapLevel(float lvl)
	{
		capTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, lvl);
	}
	
	public void addChargeCurrent(float cur)
	{
		chgTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, cur);
	}
}
