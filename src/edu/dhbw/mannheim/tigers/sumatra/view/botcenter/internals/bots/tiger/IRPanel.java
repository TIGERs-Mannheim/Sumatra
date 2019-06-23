/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.01.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DLtdReplacing;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;


/**
 * Acceleration monitoring.
 * 
 * @author AndreR
 * 
 */
public class IRPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -8510239984213940658L;
	
	private final Chart2D		posChart				= new Chart2D();
	private final Chart2D		barrierChart		= new Chart2D();
	
	private final Chart2D		barChart				= new Chart2D();
	
	private static final int	DATA_SIZE			= 400;
	
	private final ITrace2D[]	voltageTraces		= new Trace2DLtd[4];
	private final ITrace2D[]	barTraces			= new Trace2DLtdReplacing[4];
	
	private final JTextField[]	voltages				= new JTextField[4];
	
	private long					timeOffset			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public IRPanel()
	{
		setLayout(new MigLayout("wrap 2", "[150]10[grow]", "[grow][grow]"));
		
		for (int i = 0; i < 4; i++)
		{
			voltageTraces[i] = new Trace2DLtd(DATA_SIZE);
			barTraces[i] = new Trace2DLtdReplacing(1);
			voltages[i] = new JTextField();
		}
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		infoPanel.add(new JLabel("Left:"));
		infoPanel.add(voltages[TigerKickerIrLog.LEFT]);
		infoPanel.add(new JLabel("Center:"));
		infoPanel.add(voltages[TigerKickerIrLog.CENTER]);
		infoPanel.add(new JLabel("Right:"));
		infoPanel.add(voltages[TigerKickerIrLog.RIGHT]);
		infoPanel.add(new JLabel("Barrier:"));
		infoPanel.add(voltages[TigerKickerIrLog.BARRIER]);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Voltages"));
		
		// Chart setup
		voltageTraces[TigerKickerIrLog.LEFT].setColor(Color.RED);
		voltageTraces[TigerKickerIrLog.LEFT].setName("Left");
		voltageTraces[TigerKickerIrLog.CENTER].setColor(Color.GREEN);
		voltageTraces[TigerKickerIrLog.CENTER].setName("Center");
		voltageTraces[TigerKickerIrLog.RIGHT].setColor(Color.BLUE);
		voltageTraces[TigerKickerIrLog.RIGHT].setName("Right");
		voltageTraces[TigerKickerIrLog.BARRIER].setColor(Color.BLACK);
		voltageTraces[TigerKickerIrLog.BARRIER].setName("Barrier");
		
		barChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 2.5)));
		barChart.getAxisX().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 5.0)));
		barChart.getAxisY().setMajorTickSpacing(10);
		barChart.getAxisY().setMinorTickSpacing(2.5);
		barChart.getAxisY().setPaintGrid(true);
		barChart.getAxisY().setAxisTitle(new AxisTitle("U [V]"));
		barChart.getAxisX().setPaintScale(false);
		barChart.getAxisX().setAxisTitle(new AxisTitle(""));
		barChart.setBackground(getBackground());
		barChart.setGridColor(Color.LIGHT_GRAY);
		
		barTraces[TigerKickerIrLog.LEFT].setColor(Color.RED);
		barTraces[TigerKickerIrLog.LEFT].setName("");
		barTraces[TigerKickerIrLog.LEFT].setTracePainter(new TracePainterVerticalBar(4, barChart));
		barTraces[TigerKickerIrLog.CENTER].setColor(Color.GREEN);
		barTraces[TigerKickerIrLog.CENTER].setName("");
		barTraces[TigerKickerIrLog.CENTER].setTracePainter(new TracePainterVerticalBar(4, barChart));
		barTraces[TigerKickerIrLog.RIGHT].setColor(Color.BLUE);
		barTraces[TigerKickerIrLog.RIGHT].setName("");
		barTraces[TigerKickerIrLog.RIGHT].setTracePainter(new TracePainterVerticalBar(4, barChart));
		barTraces[TigerKickerIrLog.BARRIER].setColor(Color.BLACK);
		barTraces[TigerKickerIrLog.BARRIER].setName("");
		barTraces[TigerKickerIrLog.BARRIER].setTracePainter(new TracePainterVerticalBar(4, barChart));
		
		barChart.addTrace(barTraces[0]);
		barChart.addTrace(barTraces[1]);
		barChart.addTrace(barTraces[2]);
		barChart.addTrace(barTraces[3]);
		
		barrierChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 2.5)));
		barrierChart.getAxisY().setAxisTitle(new AxisTitle(""));
		barrierChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		barrierChart.getAxisX().setMajorTickSpacing(10);
		barrierChart.getAxisX().setMinorTickSpacing(10);
		barrierChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		barrierChart.setBackground(getBackground());
		barrierChart.addTrace(voltageTraces[TigerKickerIrLog.BARRIER]);
		
		posChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 2.5)));
		posChart.getAxisY().setAxisTitle(new AxisTitle(""));
		posChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		posChart.getAxisX().setMajorTickSpacing(10);
		posChart.getAxisX().setMinorTickSpacing(10);
		posChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		posChart.setBackground(getBackground());
		posChart.addTrace(voltageTraces[TigerKickerIrLog.LEFT]);
		posChart.addTrace(voltageTraces[TigerKickerIrLog.CENTER]);
		posChart.addTrace(voltageTraces[TigerKickerIrLog.RIGHT]);
		
		infoPanel.add(barChart, "spanx 2, grow, h 250");
		
		add(infoPanel, "spany 2, aligny top");
		add(posChart, "grow, pushy");
		add(barrierChart, "grow");
		
		barTraces[TigerKickerIrLog.LEFT].addPoint(1.0, 0.5);
		barTraces[TigerKickerIrLog.CENTER].addPoint(2.0, 1.0);
		barTraces[TigerKickerIrLog.RIGHT].addPoint(3.0, 1.2);
		barTraces[TigerKickerIrLog.BARRIER].addPoint(4.0, 0.2);
		
		timeOffset = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param log
	 * 
	 */
	public void addIrLog(final TigerKickerIrLog log)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < 4; i++)
				{
					voltageTraces[i].addPoint((System.nanoTime() - timeOffset) / 1000000000.0, log.getVoltage()[i]);
					voltages[i].setText(String.format(Locale.ENGLISH, "%1.4f V", log.getVoltage()[i]));
				}
				
				barTraces[TigerKickerIrLog.LEFT].addPoint(1.0, log.getVoltage()[TigerKickerIrLog.LEFT]);
				barTraces[TigerKickerIrLog.CENTER].addPoint(2.0, log.getVoltage()[TigerKickerIrLog.CENTER]);
				barTraces[TigerKickerIrLog.RIGHT].addPoint(3.0, log.getVoltage()[TigerKickerIrLog.RIGHT]);
				barTraces[TigerKickerIrLog.BARRIER].addPoint(4.0, log.getVoltage()[TigerKickerIrLog.BARRIER]);
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
