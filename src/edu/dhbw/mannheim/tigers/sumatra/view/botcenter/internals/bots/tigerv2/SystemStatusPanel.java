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
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusExt;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;


/**
 * Displays system status messages content.
 * 
 * @author AndreR
 * 
 */
public class SystemStatusPanel extends JPanel
{
	/** */
	public interface ISystemStatusPanelObserver
	{
		/**
		 * @param capture
		 */
		void onCaptureMovementData(boolean capture);
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID		= -7760159178483321304L;
	
	private final JTextField								kickerLevel				= new JTextField();
	private final JCheckBox									barrierInterrupted	= new JCheckBox("Interrupted");
	private final JCheckBox									dribblerReached		= new JCheckBox("Speed reached");
	private final JCheckBox									dribblerOverload		= new JCheckBox("Overload");
	private final JCheckBox									stateUpdated[]			= new JCheckBox[3];
	private final JTextField								pos[]						= new JTextField[3];
	private final JTextField								vel[]						= new JTextField[3];
	private final JTextField								acc[]						= new JTextField[3];
	
	private final Chart2D									posChart					= new Chart2D();
	private final Chart2D									velChart					= new Chart2D();
	private final Chart2D									accChart					= new Chart2D();
	private final Chart2D									targetPosChart			= new Chart2D();
	private final Chart2D									targetVelChart			= new Chart2D();
	private final Chart2D									targetAccChart			= new Chart2D();
	private final Chart2D									motorChart				= new Chart2D();
	private final Chart2D									motorVelChart			= new Chart2D();
	private final List<Chart2D>							chartList				= new ArrayList<Chart2D>(6);
	
	private static final int								DATA_SIZE				= 400;
	
	private final ITrace2D									posXTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									posYTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									posWTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									velXTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									velYTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									velWTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									accXTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									accYTrace				= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									accWTrace				= new Trace2DLtd(DATA_SIZE);
	
	private final ITrace2D									targetPosXTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetPosYTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetPosWTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetVelXTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetVelYTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetVelWTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetAccXTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetAccYTrace		= new Trace2DLtd(DATA_SIZE);
	private final ITrace2D									targetAccWTrace		= new Trace2DLtd(DATA_SIZE);
	
	private final List<ITrace2D>							motorTraces				= new ArrayList<ITrace2D>(4);
	private final List<ITrace2D>							motorVelTraces			= new ArrayList<ITrace2D>(4);
	
	private final JToggleButton							btnCapture				= new JToggleButton("Capture");
	
	private long												timeOffset				= 0;
	
	
	private final List<ISystemStatusPanelObserver>	observers				= new CopyOnWriteArrayList<ISystemStatusPanelObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(ISystemStatusPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(ISystemStatusPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyCapture(boolean capture)
	{
		synchronized (observers)
		{
			for (ISystemStatusPanelObserver observer : observers)
			{
				observer.onCaptureMovementData(capture);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default constructor.
	 */
	public SystemStatusPanel()
	{
		setLayout(new MigLayout("wrap 2"));
		
		btnCapture.addActionListener(new CaptureActionListener());
		
		chartList.add(posChart);
		chartList.add(velChart);
		chartList.add(accChart);
		chartList.add(targetPosChart);
		chartList.add(targetVelChart);
		chartList.add(targetAccChart);
		chartList.add(motorChart);
		chartList.add(motorVelChart);
		
		posChart.setName("pos");
		velChart.setName("vel");
		accChart.setName("acc");
		targetPosChart.setName("setVel");
		targetVelChart.setName("outVel");
		targetAccChart.setName("errorVel");
		motorChart.setName("motor");
		motorVelChart.setName("motorVel");
		
		motorTraces.add(new Trace2DLtd(DATA_SIZE));
		motorTraces.add(new Trace2DLtd(DATA_SIZE));
		motorTraces.add(new Trace2DLtd(DATA_SIZE));
		motorTraces.add(new Trace2DLtd(DATA_SIZE));
		motorVelTraces.add(new Trace2DLtd(DATA_SIZE));
		motorVelTraces.add(new Trace2DLtd(DATA_SIZE));
		motorVelTraces.add(new Trace2DLtd(DATA_SIZE));
		motorVelTraces.add(new Trace2DLtd(DATA_SIZE));
		
		// Chart setup
		posXTrace.setColor(Color.RED);
		posXTrace.setName("pX");
		posYTrace.setColor(Color.GREEN);
		posYTrace.setName("pY");
		posWTrace.setColor(Color.BLUE);
		posWTrace.setName("pW");
		velXTrace.setColor(Color.RED);
		velXTrace.setName("vX");
		velYTrace.setColor(Color.GREEN);
		velYTrace.setName("vY");
		velWTrace.setColor(Color.BLUE);
		velWTrace.setName("vW");
		accXTrace.setColor(Color.RED);
		accXTrace.setName("aX");
		accYTrace.setColor(Color.GREEN);
		accYTrace.setName("aY");
		accWTrace.setColor(Color.BLUE);
		accWTrace.setName("aW");
		
		targetPosXTrace.setColor(Color.RED.darker());
		targetPosXTrace.setName("d1X");
		targetPosYTrace.setColor(Color.GREEN.darker());
		targetPosYTrace.setName("d1pY");
		targetPosWTrace.setColor(Color.BLUE.darker());
		targetPosWTrace.setName("d1W");
		targetVelXTrace.setColor(Color.RED.darker());
		targetVelXTrace.setName("d2X");
		targetVelYTrace.setColor(Color.GREEN.darker());
		targetVelYTrace.setName("d2Y");
		targetVelWTrace.setColor(Color.BLUE.darker());
		targetVelWTrace.setName("d2W");
		targetAccXTrace.setColor(Color.RED.darker());
		targetAccXTrace.setName("d3X");
		targetAccYTrace.setColor(Color.GREEN.darker());
		targetAccYTrace.setName("d3Y");
		targetAccWTrace.setColor(Color.BLUE.darker());
		targetAccWTrace.setName("d3W");
		
		motorTraces.get(0).setColor(Color.BLUE);
		motorTraces.get(0).setName("M1");
		motorTraces.get(1).setColor(Color.GREEN);
		motorTraces.get(1).setName("M2");
		motorTraces.get(2).setColor(Color.RED);
		motorTraces.get(2).setName("M3");
		motorTraces.get(3).setColor(Color.MAGENTA);
		motorTraces.get(3).setName("M4");
		
		motorVelTraces.get(0).setColor(Color.BLUE);
		motorVelTraces.get(0).setName("velM1");
		motorVelTraces.get(1).setColor(Color.GREEN);
		motorVelTraces.get(1).setName("velM2");
		motorVelTraces.get(2).setColor(Color.RED);
		motorVelTraces.get(2).setName("velM3");
		motorVelTraces.get(3).setColor(Color.MAGENTA);
		motorVelTraces.get(3).setName("velM4");
		
		for (Chart2D chart : chartList)
		{
			// chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(-5.0, 5.0)));
			chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(-1, 1)));
			chart.getAxisX().setRangePolicy(new RangePolicyHighestValues(20));
			chart.getAxisX().setMajorTickSpacing(10);
			chart.getAxisX().setMinorTickSpacing(10);
			chart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
			chart.setBackground(getBackground());
		}
		
		posChart.addTrace(posXTrace);
		posChart.addTrace(posYTrace);
		posChart.addTrace(posWTrace);
		targetPosChart.addTrace(targetPosXTrace);
		targetPosChart.addTrace(targetPosYTrace);
		targetPosChart.addTrace(targetPosWTrace);
		
		velChart.addTrace(velXTrace);
		velChart.addTrace(velYTrace);
		velChart.addTrace(velWTrace);
		targetVelChart.addTrace(targetVelXTrace);
		targetVelChart.addTrace(targetVelYTrace);
		targetVelChart.addTrace(targetVelWTrace);
		
		accChart.addTrace(accXTrace);
		accChart.addTrace(accYTrace);
		accChart.addTrace(accWTrace);
		targetAccChart.addTrace(targetAccXTrace);
		targetAccChart.addTrace(targetAccYTrace);
		targetAccChart.addTrace(targetAccWTrace);
		
		for (int i = 0; i < 4; i++)
		{
			motorChart.addTrace(motorTraces.get(i));
			motorVelChart.addTrace(motorVelTraces.get(i));
		}
		
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
		
		final JPanel chkBoxPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		for (Chart2D chart : chartList)
		{
			chkBoxPanel.add(createChartCheckBox(chart));
		}
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 1"));
		
		infoPanel.add(btnCapture);
		infoPanel.add(kickerPanel);
		infoPanel.add(posPanel);
		infoPanel.add(velPanel);
		infoPanel.add(accPanel);
		infoPanel.add(chkBoxPanel);
		
		add(infoPanel, "spany 8, aligny top");
		add(posChart, "push, grow");
		add(velChart, "push, grow");
		add(accChart, "push, grow");
		add(targetPosChart, "push, grow");
		add(targetVelChart, "push, grow");
		add(targetAccChart, "push, grow");
		add(motorChart, "push, grow");
		add(motorVelChart, "push, grow");
		
		timeOffset = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private JCheckBox createChartCheckBox(final Chart2D chart)
	{
		final JCheckBox chkBox = new JCheckBox(chart.getName(), true);
		chkBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (chkBox.isSelected())
				{
					chart.setEnabled(true);
					add(chart, "push, grow");
				} else
				{
					chart.setEnabled(false);
					remove(chart);
				}
				invalidate();
			}
		});
		return chkBox;
	}
	
	
	/**
	 * Add a system status to display.
	 * 
	 * @param status
	 */
	public void addSystemStatusV2(final TigerSystemStatusV2 status)
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
		
		acc[0].setText(String.format(Locale.ENGLISH, "%.3f m/s2", status.getAcceleration().x));
		acc[1].setText(String.format(Locale.ENGLISH, "%.3f m/s2", status.getAcceleration().y));
		acc[2].setText(String.format(Locale.ENGLISH, "%.3f rad/s2", status.getAngularAcceleration()));
		
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
	
	
	/**
	 * @param status
	 */
	public void addSystemStatusExt(final TigerSystemStatusExt status)
	{
		double time = (System.nanoTime() - timeOffset) / 1000000000.0;
		targetPosXTrace.addPoint(time, status.getTargetPosition().x);
		targetPosYTrace.addPoint(time, status.getTargetPosition().y);
		targetPosWTrace.addPoint(time, status.getTargetOrientation());
		
		targetVelXTrace.addPoint(time, status.getTargetVelocity().x);
		targetVelYTrace.addPoint(time, status.getTargetVelocity().y);
		targetVelWTrace.addPoint(time, status.getTargetAngularVelocity());
		
		targetAccXTrace.addPoint(time, status.getTargetAcceleration().x);
		targetAccYTrace.addPoint(time, status.getTargetAcceleration().y);
		targetAccWTrace.addPoint(time, status.getTargetAngularAcceleration());
		
		for (int i = 0; i < 4; i++)
		{
			motorTraces.get(i).addPoint(time, status.getMotor()[i]);
			motorVelTraces.get(i).addPoint(time, status.getMotorVel()[i]);
		}
	}
	
	private class CaptureActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyCapture(btnCapture.isSelected());
		}
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
