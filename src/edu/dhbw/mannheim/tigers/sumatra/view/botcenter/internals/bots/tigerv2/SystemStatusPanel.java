/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;


/**
 * Displays system status messages content.
 * 
 * @author AndreR
 * 
 */
public class SystemStatusPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID		= -7760159178483321304L;
	
	private final JTextField	kickerLevel				= new JTextField();
	private final JCheckBox		barrierInterrupted	= new JCheckBox("Interrupted");
	private final JCheckBox		dribblerReached		= new JCheckBox("Speed reached");
	private final JCheckBox		dribblerOverload		= new JCheckBox("Overload");
	private final JCheckBox		stateUpdated[]			= new JCheckBox[3];
	private final JTextField	pos[]						= new JTextField[3];
	private final JTextField	vel[]						= new JTextField[3];
	private final JTextField	acc[]						= new JTextField[3];
	
	private final Chart2D		posChart					= new Chart2D();
	private final Chart2D		velChart					= new Chart2D();
	private final Chart2D		accChart					= new Chart2D();
	
	private static final int	DATA_SIZE				= 400;
	
	private final ITrace2D		posXTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		posYTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		posWTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		velXTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		velYTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		velWTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		accXTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		accYTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D		accWTrace				= new Trace2DLtd(DATA_SIZE);
	
	private long					timeOffset				= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default constructor.
	 */
	public SystemStatusPanel()
	{
		setLayout(new MigLayout("wrap 2"));
		
		// Chart setup
		posXTrace.setColor(Color.RED);
		posXTrace.setName("X");
		posYTrace.setColor(Color.GREEN);
		posYTrace.setName("Y");
		posWTrace.setColor(Color.BLUE);
		posWTrace.setName("W");
		velXTrace.setColor(Color.RED);
		velXTrace.setName("X");
		velYTrace.setColor(Color.GREEN);
		velYTrace.setName("Y");
		velWTrace.setColor(Color.BLUE);
		velWTrace.setName("W");
		accXTrace.setColor(Color.RED);
		accXTrace.setName("X");
		accYTrace.setColor(Color.GREEN);
		accYTrace.setName("Y");
		accWTrace.setColor(Color.BLUE);
		accWTrace.setName("W");
		
		posChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-5.0, 5.0)));
		posChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		posChart.getAxisX().setMajorTickSpacing(10);
		posChart.getAxisX().setMinorTickSpacing(10);
		posChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		posChart.setBackground(getBackground());
		posChart.addTrace(posXTrace);
		posChart.addTrace(posYTrace);
		posChart.addTrace(posWTrace);
		
		velChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-5.0, 5.0)));
		velChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		velChart.getAxisX().setMajorTickSpacing(10);
		velChart.getAxisX().setMinorTickSpacing(10);
		velChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		velChart.setBackground(getBackground());
		velChart.addTrace(velXTrace);
		velChart.addTrace(velYTrace);
		velChart.addTrace(velWTrace);
		
		accChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-5.0, 5.0)));
		accChart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		accChart.getAxisX().setMajorTickSpacing(10);
		accChart.getAxisX().setMinorTickSpacing(10);
		accChart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		accChart.setBackground(getBackground());
		accChart.addTrace(accXTrace);
		accChart.addTrace(accYTrace);
		accChart.addTrace(accWTrace);
		
		
		for (int i = 0; i < 3; i++)
		{
			stateUpdated[i] = new JCheckBox("Updated");
			pos[i] = new JTextField();
			vel[i] = new JTextField();
			acc[i] = new JTextField();
		}
		
		final JPanel posPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		posPanel.add(new JLabel("X:"));
		posPanel.add(pos[0]);
		posPanel.add(new JLabel("Y:"));
		posPanel.add(pos[1]);
		posPanel.add(new JLabel("W:"));
		posPanel.add(pos[2]);
		posPanel.add(stateUpdated[0], "spanx 2");
		posPanel.setBorder(BorderFactory.createTitledBorder("Position"));
		
		final JPanel velPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		velPanel.add(new JLabel("X:"));
		velPanel.add(vel[0]);
		velPanel.add(new JLabel("Y:"));
		velPanel.add(vel[1]);
		velPanel.add(new JLabel("W:"));
		velPanel.add(vel[2]);
		velPanel.add(stateUpdated[1], "spanx 2");
		velPanel.setBorder(BorderFactory.createTitledBorder("Velocity"));
		
		final JPanel accPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		accPanel.add(new JLabel("X:"));
		accPanel.add(acc[0]);
		accPanel.add(new JLabel("Y:"));
		accPanel.add(acc[1]);
		accPanel.add(new JLabel("W:"));
		accPanel.add(acc[2]);
		accPanel.add(stateUpdated[2], "spanx 2");
		accPanel.setBorder(BorderFactory.createTitledBorder("Acceleration"));
		
		final JPanel kickerPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		kickerPanel.add(new JLabel("Kicker:"));
		kickerPanel.add(kickerLevel);
		kickerPanel.add(new JLabel("Barrier:"));
		kickerPanel.add(barrierInterrupted);
		kickerPanel.add(new JLabel("Dribbler:"));
		kickerPanel.add(dribblerReached);
		kickerPanel.add(new JLabel("Dribbler:"));
		kickerPanel.add(dribblerOverload);
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 1"));
		
		infoPanel.add(kickerPanel);
		infoPanel.add(posPanel);
		infoPanel.add(velPanel);
		infoPanel.add(accPanel);
		
		add(infoPanel, "spany 3, aligny top");
		add(posChart, "push, grow");
		add(velChart, "push, grow");
		add(accChart, "push, grow");
		
		timeOffset = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Add a system status to display.
	 * 
	 * @param status
	 */
	public void addSystemStatusV2(final TigerSystemStatusV2 status)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				kickerLevel.setText(String.format(Locale.ENGLISH, "%.1f V", status.getKickerLevel()));
				barrierInterrupted.setSelected(status.isBarrierInterrupted());
				dribblerReached.setSelected(status.isDribblerSpeedReached());
				dribblerOverload.setSelected(status.isDribblerOverloaded());
				
				stateUpdated[0].setSelected(status.isPositionUpdated());
				stateUpdated[1].setSelected(status.isVelocityUpdated());
				stateUpdated[2].setSelected(status.isAccelerationUpdated());
				
				pos[0].setText(String.format(Locale.ENGLISH, "%.3f m", status.getPosition().x));
				pos[1].setText(String.format(Locale.ENGLISH, "%.3f m", status.getPosition().y));
				pos[2].setText(String.format(Locale.ENGLISH, "%.3f rad", status.getOrientation()));
				
				vel[0].setText(String.format(Locale.ENGLISH, "%.3f m/s", status.getVelocity().x));
				vel[1].setText(String.format(Locale.ENGLISH, "%.3f m/s", status.getVelocity().y));
				vel[2].setText(String.format(Locale.ENGLISH, "%.3f rad/s", status.getAngularVelocity()));
				
				acc[0].setText(String.format(Locale.ENGLISH, "%.3f m/s²", status.getAcceleration().x));
				acc[1].setText(String.format(Locale.ENGLISH, "%.3f m/s²", status.getAcceleration().y));
				acc[2].setText(String.format(Locale.ENGLISH, "%.3f rad/s²", status.getAngularAcceleration()));
				
				posXTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getPosition().x);
				posYTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getPosition().y);
				posWTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getOrientation());
				
				velXTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getVelocity().x);
				velYTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getVelocity().y);
				velWTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getAngularVelocity());
				
				accXTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getAcceleration().x);
				accYTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getAcceleration().y);
				accWTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getAngularAcceleration());
			}
		});
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
