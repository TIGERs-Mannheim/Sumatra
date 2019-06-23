/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker;

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

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;


/**
 * Manual kicker firing control
 * 
 * @author AndreR
 * 
 */
public class KickerFirePanelV2 extends JPanel
{
	/**
	 */
	public interface IKickerFirePanelObserverV2
	{
		/**
		 * @param duration
		 * @param mode
		 * @param device
		 */
		void onKickerFire(float duration, int mode, int device);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= 7195056304680630611L;
	
	private final JTextField								duration;
	private final JRadioButton								modeForce;
	private final JRadioButton								modeArm;
	private final JRadioButton								modeDisarm;
	private final JRadioButton								modeDribbler;
	private final JRadioButton								deviceStraight;
	private final JRadioButton								deviceChip;
	
	private final List<IKickerFirePanelObserverV2>	observers			= new ArrayList<IKickerFirePanelObserverV2>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KickerFirePanelV2()
	{
		setLayout(new MigLayout("fill"));
		
		duration = new JTextField("10000");
		modeForce = new JRadioButton("Force");
		modeArm = new JRadioButton("Arm");
		modeDisarm = new JRadioButton("Disarm");
		modeDribbler = new JRadioButton("Dribbler");
		deviceStraight = new JRadioButton("Straight");
		deviceChip = new JRadioButton("Chip");
		
		final JButton fire = new JButton("Fire!");
		fire.addActionListener(new Fire());
		
		final ButtonGroup mode = new ButtonGroup();
		mode.add(modeForce);
		mode.add(modeArm);
		mode.add(modeDribbler);
		mode.add(modeDisarm);
		
		final ButtonGroup device = new ButtonGroup();
		device.add(deviceStraight);
		device.add(deviceChip);
		
		final JPanel modePanel = new JPanel(new MigLayout("fill"));
		modePanel.add(modeForce, "wrap");
		modePanel.add(modeArm, "wrap");
		modePanel.add(modeDisarm, "wrap");
		modePanel.add(modeDribbler, "wrap");
		modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
		modeForce.setSelected(true);
		
		final JPanel devicePanel = new JPanel(new MigLayout(""));
		devicePanel.add(deviceStraight, "wrap");
		devicePanel.add(deviceChip, "wrap");
		devicePanel.setBorder(BorderFactory.createTitledBorder("Device"));
		deviceStraight.setSelected(true);
		
		final JPanel controlPanel = new JPanel(new MigLayout("fill"));
		controlPanel.add(new JLabel("Duration:"));
		controlPanel.add(duration, "w 100");
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
	public void addObserver(IKickerFirePanelObserverV2 observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IKickerFirePanelObserverV2 observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyFire(float duration, int mode, int device)
	{
		synchronized (observers)
		{
			for (final IKickerFirePanelObserverV2 observer : observers)
			{
				observer.onKickerFire(duration, mode, device);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public class Fire implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			float dur = 0;
			int m = 0;
			int dev = 0;
			
			try
			{
				dur = Float.parseFloat(duration.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			if (modeForce.isSelected())
			{
				m = TigerKickerKickV2.Mode.FORCE;
			}
			
			if (modeArm.isSelected())
			{
				m = TigerKickerKickV2.Mode.ARM;
			}
			
			if (modeDisarm.isSelected())
			{
				m = TigerKickerKickV2.Mode.DISARM;
			}
			
			if (modeDribbler.isSelected())
			{
				m = TigerKickerKickV2.Mode.DRIBBLER;
			}
			
			if (deviceStraight.isSelected())
			{
				dev = TigerKickerKickV2.Device.STRAIGHT;
			}
			
			if (deviceChip.isSelected())
			{
				dev = TigerKickerKickV2.Device.CHIP;
			}
			
			notifyFire(dur, m, dev);
		}
	}
}
