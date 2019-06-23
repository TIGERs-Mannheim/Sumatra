/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.10.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

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
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;


/**
 * Power monitoring.
 * 
 * @author AndreR
 * 
 */
public class PowerPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 6973741000708311434L;
	
	private final Chart2D		iVccChart			= new Chart2D();
	private final Chart2D		i5VChart				= new Chart2D();
	
	private static final int	DATA_SIZE			= 400;
	
	private final ITrace2D		iVccTrace			= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		i5VTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		i3V3Trace			= new Trace2DLtd(DATA_SIZE);
	
	private JTextField			iVcc					= null;
	private JTextField			i5V					= null;
	private JTextField			i3V3					= null;
	private JProgressBar			uCell1				= null;
	private JProgressBar			uCell2				= null;
	private JProgressBar			uCell3				= null;
	private JProgressBar			uCell4				= null;
	private JProgressBar			uPower				= null;
	
	private long					timeOffset			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PowerPanel()
	{
		setLayout(new MigLayout("wrap 2"));
		
		iVcc = new JTextField();
		i5V = new JTextField();
		i3V3 = new JTextField();
		uCell1 = new JProgressBar(3300, 4200);
		uCell2 = new JProgressBar(3300, 4200);
		uCell3 = new JProgressBar(3300, 4200);
		uCell4 = new JProgressBar(3300, 4200);
		uPower = new JProgressBar(13200, 16800);
		uCell1.setStringPainted(true);
		uCell2.setStringPainted(true);
		uCell3.setStringPainted(true);
		uCell4.setStringPainted(true);
		uPower.setStringPainted(true);
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 1"));
		
		final JPanel currentPanel = new JPanel(new MigLayout("fill, wrap 3", "[50]10[100,fill]10[20]"));
		currentPanel.add(new JLabel("VCC:"));
		currentPanel.add(iVcc);
		currentPanel.add(new JLabel("A"));
		currentPanel.add(new JLabel("5V:"));
		currentPanel.add(i5V);
		currentPanel.add(new JLabel("A"));
		currentPanel.add(new JLabel("3.3V:"));
		currentPanel.add(i3V3);
		currentPanel.add(new JLabel("A"));
		currentPanel.setBorder(BorderFactory.createTitledBorder("Currents"));
		
		final String rowConstraints = "[50]10[100,fill]10[20]";
		final String columnConstraints = "[20,fill][20,fill][20,fill][20,fill][20,fill]";
		final JPanel voltagePanel = new JPanel(new MigLayout("fill, wrap 3", rowConstraints, columnConstraints));
		voltagePanel.add(new JLabel("Cell 1:"));
		voltagePanel.add(uCell1);
		voltagePanel.add(new JLabel("V"));
		voltagePanel.add(new JLabel("Cell 2:"));
		voltagePanel.add(uCell2);
		voltagePanel.add(new JLabel("V"));
		voltagePanel.add(new JLabel("Cell 3:"));
		voltagePanel.add(uCell3);
		voltagePanel.add(new JLabel("V"));
		voltagePanel.add(new JLabel("Cell 4:"));
		voltagePanel.add(uCell4);
		voltagePanel.add(new JLabel("V"));
		voltagePanel.add(new JLabel("Overall:"));
		voltagePanel.add(uPower);
		voltagePanel.add(new JLabel("V"));
		voltagePanel.setBorder(BorderFactory.createTitledBorder("Battery"));
		
		infoPanel.add(voltagePanel);
		infoPanel.add(currentPanel);
		
		// Chart setup
		iVccTrace.setColor(Color.RED);
		iVccTrace.setName("Vcc");
		i5VTrace.setColor(Color.RED);
		i5VTrace.setName("5V");
		i3V3Trace.setColor(Color.BLUE);
		i3V3Trace.setName("3.3V");
		
		final AAxis<AxisScalePolicyAutomaticBestFit> currentAxis = new AxisLinear<AxisScalePolicyAutomaticBestFit>();
		currentAxis.setRange(new Range(0, 1.0));
		
		i5VChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 1.0)));
		i5VChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(60));
		i5VChart.getAxisX().setMajorTickSpacing(10);
		i5VChart.getAxisX().setMinorTickSpacing(10);
		i5VChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		i5VChart.addAxisYRight(currentAxis);
		currentAxis.setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 1.0)));
		i5VChart.setBackground(getBackground());
		i5VChart.addTrace(i5VTrace);
		i5VChart.addTrace(i3V3Trace, i5VChart.getAxisX(), currentAxis);
		
		iVccChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 20.0)));
		iVccChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(60));
		iVccChart.getAxisX().setMajorTickSpacing(10);
		iVccChart.getAxisX().setMinorTickSpacing(10);
		iVccChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		iVccChart.setBackground(getBackground());
		iVccChart.addTrace(iVccTrace);
		
		add(infoPanel, "spany 2, aligny top");
		add(iVccChart, "push, grow");
		add(i5VChart, "push, grow");
		
		timeOffset = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param log
	 */
	public void addPowerLog(final TigerSystemPowerLog log)
	{
		iVccTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, log.getiVCC());
		i5VTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, log.getI5V());
		i3V3Trace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, log.getI3V3());
		
		iVcc.setText(String.format(Locale.ENGLISH, "%1.3f", log.getiVCC()));
		i5V.setText(String.format(Locale.ENGLISH, "%1.3f", log.getI5V()));
		i3V3.setText(String.format(Locale.ENGLISH, "%1.3f", log.getI3V3()));
		
		uCell1.setString(String.format(Locale.ENGLISH, "%1.3f", log.getU(0)));
		uCell2.setString(String.format(Locale.ENGLISH, "%1.3f", log.getU(1)));
		uCell3.setString(String.format(Locale.ENGLISH, "%1.3f", log.getU(2)));
		uCell4.setString(String.format(Locale.ENGLISH, "%1.3f", log.getU(3)));
		uPower.setString(String.format(Locale.ENGLISH, "%1.3f", log.getU(0) + log.getU(1) + log.getU(2) + log.getU(3)));
		
		uCell1.setValue((int) (log.getU(0) * 1000));
		uCell2.setValue((int) (log.getU(1) * 1000));
		uCell3.setValue((int) (log.getU(2) * 1000));
		uCell4.setValue((int) (log.getU(3) * 1000));
		uPower.setValue((int) ((log.getU(0) + log.getU(1) + log.getU(2) + log.getU(3)) * 1000));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
