/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.09.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

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
public class KickerFirePanel extends JPanel
{
	public interface IKickerFirePanelObserver
	{
		public void onKickerFire(int level, float duration, int mode, int device);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 7195056304680630611L;
	
	private JTextField duration;
	private JTextField level;
	private JRadioButton modeForce;
	private JRadioButton modeArm;
	private JRadioButton modeDisarm;
	private JRadioButton deviceStraight;
	private JRadioButton deviceChip;
	
	private final List<IKickerFirePanelObserver> observers = new ArrayList<IKickerFirePanelObserver>();
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KickerFirePanel()
	{
		setLayout(new MigLayout("fill"));
		
		duration = new JTextField();
		level = new JTextField();
		modeForce = new JRadioButton("Force");
		modeArm = new JRadioButton("Arm");
		modeDisarm = new JRadioButton("Disarm");
		deviceStraight = new JRadioButton("Straight");
		deviceChip = new JRadioButton("Chip");
		
		JButton fire = new JButton("Fire!");
		fire.addActionListener(new Fire());
		
		ButtonGroup mode = new ButtonGroup();
		mode.add(modeForce);
		mode.add(modeArm);
		mode.add(modeDisarm);
		
		ButtonGroup device = new ButtonGroup();
		device.add(deviceStraight);
		device.add(deviceChip);
		
		JPanel modePanel = new JPanel(new MigLayout("fill"));
		modePanel.add(modeForce, "wrap");
		modePanel.add(modeArm, "wrap");
		modePanel.add(modeDisarm, "wrap");
		modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
		modeForce.setSelected(true);
		
		JPanel devicePanel = new JPanel(new MigLayout(""));
		devicePanel.add(deviceStraight, "wrap");
		devicePanel.add(deviceChip, "wrap");
		devicePanel.setBorder(BorderFactory.createTitledBorder("Device"));
		deviceStraight.setSelected(true);
		
		JPanel controlPanel = new JPanel(new MigLayout("fill"));
		controlPanel.add(new JLabel("Duration:"));
		controlPanel.add(duration, "w 100");
		controlPanel.add(new JLabel("us"));
		controlPanel.add(fire, "wrap");
		controlPanel.add(new JLabel("Level:"));
		controlPanel.add(level, "w 100, span 3");
		
		add(controlPanel, "span 2, wrap");
		add(modePanel);
		add(devicePanel);
		
		setBorder(BorderFactory.createTitledBorder("Fire Control"));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IKickerFirePanelObserver observer)
	{
		synchronized(observers)
		{
			observers.add(observer);
		}
	}
	
	
	public void removeObserver(IKickerFirePanelObserver observer)
	{
		synchronized(observers)
		{
			observers.remove(observer);
		}
	}
	
	private void notifyFire(int level, float duration, int mode, int device)
	{
		synchronized(observers)
		{
			for (IKickerFirePanelObserver observer : observers)
			{
				observer.onKickerFire(level, duration, mode, device);
			}
		}
	}
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public class Fire implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			float dur = 0;
			int m = 0;
			int dev = 0;
			int lvl = 0;
			
			try
			{
				dur = Float.parseFloat(duration.getText());
				lvl = Integer.parseInt(level.getText());
			}
			catch(NumberFormatException e)
			{
				return;
			}
			
			if(modeForce.isSelected())
			{
				m = TigerKickerKickV2.Mode.FORCE;
			}
			
			if(modeArm.isSelected())
			{
				m = TigerKickerKickV2.Mode.ARM;
			}
			
			if(modeDisarm.isSelected())
			{
				m = TigerKickerKickV2.Mode.DISARM;
			}
			
			if(deviceStraight.isSelected())
			{
				dev = TigerKickerKickV2.Device.STRAIGHT;
			}
			
			if(deviceChip.isSelected())
			{
				dev = TigerKickerKickV2.Device.CHIP;
			}
			
			notifyFire(lvl, dur, m, dev);
		}
	}
}
