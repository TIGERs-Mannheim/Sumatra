/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyAutomaticBestFit;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Plots motor setpoint, current, p-i-d-error
 * 
 * @author AndreR
 * 
 */
public class MotorPidPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -5719721842971377089L;
	
	private final Chart2D		targetChart			= new Chart2D();
	private final Chart2D		errorChart			= new Chart2D();
	
	private static final int	DATA_SIZE			= 400;
	
	private final ITrace2D		setpoint				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		output				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		latest				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		pError				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		iError				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		dError				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		current				= new Trace2DLtd(DATA_SIZE);
	
	private long					timeOffset			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public MotorPidPanel()
	{
		setLayout(new MigLayout("fill, wrap 1", "", ""));
		
		setpoint.setName("Setpoint");
		setpoint.setColor(Color.BLUE);
		latest.setName("Latest");
		latest.setColor(Color.RED);
		current.setName("Current");
		current.setColor(Color.GREEN);
		pError.setName("P Error");
		pError.setColor(Color.RED);
		iError.setName("I Error");
		iError.setColor(Color.GREEN);
		dError.setName("D Error");
		dError.setColor(Color.BLUE);
		output.setColor(Color.BLACK);
		output.setName("Output");
		
		final AAxis<AxisScalePolicyAutomaticBestFit> currentAxis = new AxisLinear<AxisScalePolicyAutomaticBestFit>();
		currentAxis.setRange(new Range(0, 1.0));
		
		targetChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-9000.0, 9000.0)));
		targetChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		targetChart.getAxisX().setMajorTickSpacing(10);
		targetChart.getAxisX().setMinorTickSpacing(10);
		targetChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		targetChart.addAxisYRight(currentAxis);
		currentAxis.setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 5.0)));
		targetChart.setBackground(getBackground());
		targetChart.addTrace(setpoint);
		targetChart.addTrace(latest);
		targetChart.addTrace(output);
		targetChart.addTrace(current, targetChart.getAxisX(), currentAxis);
		
		errorChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-9000.0, 9000.0)));
		errorChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		errorChart.getAxisX().setMajorTickSpacing(10);
		errorChart.getAxisX().setMinorTickSpacing(10);
		errorChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		errorChart.setBackground(getBackground());
		errorChart.addTrace(pError);
		errorChart.addTrace(iError);
		errorChart.addTrace(dError);
		
		add(targetChart, "grow");
		add(errorChart, "grow");
		
		timeOffset = SumatraClock.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param log
	 */
	public void setLog(final TigerMotorPidLog log)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				latest.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getLatest());
				setpoint.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getSetpoint());
				output.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getOutput());
				pError.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getpError());
				iError.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getiError());
				dError.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getdError());
				current.addPoint((SumatraClock.nanoTime() - timeOffset) / 1000000000.0, log.getECurrent());
			}
		});
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
