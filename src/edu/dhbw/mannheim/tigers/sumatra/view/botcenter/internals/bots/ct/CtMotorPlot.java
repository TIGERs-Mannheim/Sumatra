/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * CT bot single plot panel.
 * 
 * @author AndreR
 * 
 */
public class CtMotorPlot extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private Chart2D speedChart = new Chart2D();
	private Chart2D pChart = new Chart2D();
	private Chart2D iChart = new Chart2D();
	private Chart2D dChart = new Chart2D();
	private ITrace2D setpointTrace = new Trace2DLtd(100);
	private ITrace2D currentTrace = new Trace2DLtd(100);
	private ITrace2D pTrace = new Trace2DLtd(100);
	private ITrace2D iTrace = new Trace2DLtd(100);
	private ITrace2D dTrace = new Trace2DLtd(100);
	
	private long timeOffset = 0;

	public CtMotorPlot(String title)
	{
		setLayout(new MigLayout("fill", "", ""));
		
		add(new JLabel(title), "grow, h 20px, gapleft 50px, wrap");
		
		setpointTrace.setColor(Color.RED);
		setpointTrace.setName("Setpoint");
		
		currentTrace.setColor(Color.BLUE);
		currentTrace.setName("Current");
		
		speedChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-1.0, 1.0)));
		speedChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		speedChart.setBackground(this.getBackground());
		speedChart.setForeground(Color.BLACK);
		speedChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		speedChart.getAxisY().setAxisTitle(new AxisTitle("v [m/s]"));
		
		speedChart.addTrace(setpointTrace);
		speedChart.addTrace(currentTrace);
		
		pTrace.setColor(Color.RED);
		pTrace.setName("p-Error");
		iTrace.setColor(Color.BLUE);
		iTrace.setName("i-Error");
		dTrace.setColor(Color.BLACK);
		dTrace.setName("d-Error");

		pChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-1.0, 1.0)));
		pChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		pChart.setBackground(this.getBackground());
		pChart.setForeground(Color.BLACK);
		pChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		pChart.getAxisY().setAxisTitle(new AxisTitle("v [m/s]"));

		iChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-1.0, 1.0)));
		iChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		iChart.setBackground(this.getBackground());
		iChart.setForeground(Color.BLACK);
		iChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		iChart.getAxisY().setAxisTitle(new AxisTitle("v [m/s]"));

		dChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-5.0, 5.0)));
		dChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		dChart.setBackground(this.getBackground());
		dChart.setForeground(Color.BLACK);
		dChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		dChart.getAxisY().setAxisTitle(new AxisTitle("v [m/s]"));

		
		pChart.addTrace(pTrace);
		iChart.addTrace(iTrace);
		dChart.addTrace(dTrace);
		
		add(speedChart, "grow, h 40%, wrap");
		add(pChart, "grow, h 20%, wrap");
		add(iChart, "grow, h 20%, wrap");
		add(dChart, "grow, h 20%");
		
		timeOffset = System.nanoTime();
	}
	
	public void addSetpoint(double setpoint)
	{
		setpointTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, setpoint);
	}
	
	public void addCurrent(double current)
	{
		currentTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, current);
	}
	
	public void addpValue(double p)
	{
		pTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, p);
	}
	
	public void addiValue(double i)
	{
		iTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, i);
	}
	
	public void adddValue(double d)
	{
		dTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, d);
	}
}
