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
import info.monitorenter.util.Range;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMovementLis3LogRaw;

/**
 * Acceleration monitoring.
 * 
 * @author AndreR
 * 
 */
public class AccelerationPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -8510239984213940658L;

	private Chart2D				outerChart			= new Chart2D();
	private Chart2D				innerChart			= new Chart2D();
	
	private static final int	DATA_SIZE			= 400;
	
	private ITrace2D				outerAxTrace		= new Trace2DLtd(DATA_SIZE);
	private ITrace2D				outerAyTrace		= new Trace2DLtd(DATA_SIZE);
	private ITrace2D				outerAzTrace		= new Trace2DLtd(DATA_SIZE);
	private ITrace2D				innerAxTrace		= new Trace2DLtd(DATA_SIZE);
	private ITrace2D				innerAyTrace		= new Trace2DLtd(DATA_SIZE);
	private ITrace2D				innerAzTrace		= new Trace2DLtd(DATA_SIZE);
	
	private JTextField			outerAx				= null;
	private JTextField			outerAy				= null;
	private JTextField			outerAz				= null;
	private JTextField			innerAx				= null;
	private JTextField			innerAy				= null;
	private JTextField			innerAz				= null;
	
	private long timeOffset = 0;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public AccelerationPanel()
	{
		setLayout(new MigLayout("wrap 2"));
		
		outerAx = new JTextField();
		outerAy = new JTextField();
		outerAz = new JTextField();
		innerAx = new JTextField();
		innerAy = new JTextField();
		innerAz = new JTextField();
		
		JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 1"));
		
		JPanel outerPanel = new JPanel(new MigLayout("fill, wrap 3", "[50]10[100,fill]10[20]"));
		outerPanel.add(new JLabel("X:"));
		outerPanel.add(outerAx);
		outerPanel.add(new JLabel("mg"));
		outerPanel.add(new JLabel("Y:"));
		outerPanel.add(outerAy);
		outerPanel.add(new JLabel("mg"));
		outerPanel.add(new JLabel("Z:"));
		outerPanel.add(outerAz);
		outerPanel.add(new JLabel("mg"));
		outerPanel.setBorder(BorderFactory.createTitledBorder("Outer"));

		JPanel innerPanel = new JPanel(new MigLayout("fill, wrap 3", "[50]10[100,fill]10[20]"));
		innerPanel.add(new JLabel("X:"));
		innerPanel.add(innerAx);
		innerPanel.add(new JLabel("mg"));
		innerPanel.add(new JLabel("Y:"));
		innerPanel.add(innerAy);
		innerPanel.add(new JLabel("mg"));
		innerPanel.add(new JLabel("Z:"));
		innerPanel.add(innerAz);
		innerPanel.add(new JLabel("mg"));
		innerPanel.setBorder(BorderFactory.createTitledBorder("Inner"));

		infoPanel.add(innerPanel);
		infoPanel.add(outerPanel);
		
		// Chart setup
		outerAxTrace.setColor(Color.RED);
		outerAxTrace.setName("X");
		outerAyTrace.setColor(Color.GREEN);
		outerAyTrace.setName("Y");
		outerAzTrace.setColor(Color.BLUE);
		outerAzTrace.setName("Z");
		innerAxTrace.setColor(Color.RED);
		innerAxTrace.setName("X");
		innerAyTrace.setColor(Color.GREEN);
		innerAyTrace.setName("Y");
		innerAzTrace.setColor(Color.BLUE);
		innerAzTrace.setName("Z");

		innerChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-2000.0, 2000.0)));
		innerChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		innerChart.getAxisX().setMajorTickSpacing(10);
		innerChart.getAxisX().setMinorTickSpacing(10);
		innerChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		innerChart.setBackground(this.getBackground());
		innerChart.addTrace(innerAxTrace);
		innerChart.addTrace(innerAyTrace);
		innerChart.addTrace(innerAzTrace);

		outerChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-2000.0, 2000.0)));
		outerChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		outerChart.getAxisX().setMajorTickSpacing(10);
		outerChart.getAxisX().setMinorTickSpacing(10);
		outerChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		outerChart.setBackground(this.getBackground());
		outerChart.addTrace(outerAxTrace);
		outerChart.addTrace(outerAyTrace);
		outerChart.addTrace(outerAzTrace);


		add(infoPanel, "spany 2, aligny top");
		add(innerChart, "push, grow");
		add(outerChart, "push, grow");
		
		timeOffset = System.nanoTime();
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addMovementLog(final TigerMovementLis3LogRaw log)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				outerAxTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, log.getOuter()[0]);
				outerAyTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, log.getOuter()[1]);
				outerAzTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, log.getOuter()[2]);

				innerAxTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, log.getInner()[0]);
				innerAyTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, log.getInner()[1]);
				innerAzTrace.addPoint((System.nanoTime()-timeOffset)/1000000000.0, log.getInner()[2]);
				
				outerAx.setText(Integer.toString(log.getOuter()[0]));
				outerAy.setText(Integer.toString(log.getOuter()[1]));
				outerAz.setText(Integer.toString(log.getOuter()[2]));
				
				innerAx.setText(Integer.toString(log.getInner()[0]));
				innerAy.setText(Integer.toString(log.getInner()[1]));
				innerAz.setText(Integer.toString(log.getInner()[2]));
			}
		});
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
