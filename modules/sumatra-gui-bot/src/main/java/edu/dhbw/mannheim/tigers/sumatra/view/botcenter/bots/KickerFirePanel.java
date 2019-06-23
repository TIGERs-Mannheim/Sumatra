/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.09.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import net.miginfocom.swing.MigLayout;


/**
 * Manual kicker firing control
 * 
 * @author AndreR
 */
public class KickerFirePanel extends JPanel
{
	/**
	 * Kicker fire panel observer.
	 */
	@FunctionalInterface
	public interface IKickerFirePanelObserver
	{
		/**
		 * @param kickSpeed
		 * @param mode
		 * @param device
		 */
		void onKickerFire(double kickSpeed, EKickerMode mode, EKickerDevice device);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= 7195056304680630611L;
	
	private final JTextField							txtKickSpeed;
	private final JRadioButton							modeForce;
	private final JRadioButton							modeArm;
	private final JRadioButton							modeArmTime;
	private final JRadioButton							modeDisarm;
	private final JRadioButton							deviceStraight;
	private final JRadioButton							deviceChip;
	
	private final List<IKickerFirePanelObserver>	observers			= new ArrayList<>();
	
	
	/** Constructor. */
	public KickerFirePanel()
	{
		setLayout(new MigLayout("fill"));
		
		txtKickSpeed = new JTextField("8");
		modeForce = new JRadioButton("Force");
		modeArm = new JRadioButton("Arm");
		modeArmTime = new JRadioButton("ArmTime");
		modeDisarm = new JRadioButton("Disarm");
		deviceStraight = new JRadioButton("Straight");
		deviceChip = new JRadioButton("Chip");
		
		final JButton fire = new JButton("Fire!");
		fire.addActionListener(new Fire());
		
		final ButtonGroup mode = new ButtonGroup();
		mode.add(modeForce);
		mode.add(modeArm);
		mode.add(modeArmTime);
		mode.add(modeDisarm);
		
		final ButtonGroup device = new ButtonGroup();
		device.add(deviceStraight);
		device.add(deviceChip);
		
		final JPanel modePanel = new JPanel(new MigLayout("fill"));
		modePanel.add(modeForce, "wrap");
		modePanel.add(modeArm, "wrap");
		modePanel.add(modeArmTime, "wrap");
		modePanel.add(modeDisarm, "wrap");
		modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
		modeForce.setSelected(true);
		
		final JPanel devicePanel = new JPanel(new MigLayout(""));
		devicePanel.add(deviceStraight, "wrap");
		devicePanel.add(deviceChip, "wrap");
		devicePanel.setBorder(BorderFactory.createTitledBorder("Device"));
		deviceStraight.setSelected(true);
		
		final JPanel controlPanel = new JPanel(new MigLayout("fill"));
		controlPanel.add(new JLabel("KickSpeed [m/s]:"));
		controlPanel.add(txtKickSpeed, "w 100");
		controlPanel.add(new JLabel("us"));
		controlPanel.add(fire, "wrap");
		
		add(controlPanel, "span 2, wrap");
		add(modePanel);
		add(devicePanel);
		
		setBorder(BorderFactory.createTitledBorder("Fire Control"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IKickerFirePanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IKickerFirePanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Fire kicker action.
	 */
	public class Fire implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			double kickSpeed = 0;
			EKickerMode m = EKickerMode.FORCE;
			EKickerDevice dev = EKickerDevice.STRAIGHT;
			
			try
			{
				kickSpeed = Double.parseDouble(txtKickSpeed.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			if (modeForce.isSelected())
			{
				m = EKickerMode.FORCE;
			}
			
			if (modeArm.isSelected())
			{
				m = EKickerMode.ARM;
			}
			
			if (modeArmTime.isSelected())
			{
				m = EKickerMode.ARM_TIME;
			}
			
			if (modeDisarm.isSelected())
			{
				m = EKickerMode.DISARM;
			}
			
			if (deviceStraight.isSelected())
			{
				dev = EKickerDevice.STRAIGHT;
			}
			
			if (deviceChip.isSelected())
			{
				dev = EKickerDevice.CHIP;
			}
			
			notifyFire(kickSpeed, m, dev);
		}
		
		
		private void notifyFire(final double kickSpeed, final EKickerMode mode, final EKickerDevice device)
		{
			synchronized (observers)
			{
				for (final IKickerFirePanelObserver observer : observers)
				{
					observer.onKickerFire(kickSpeed, mode, device);
				}
			}
		}
	}
}
