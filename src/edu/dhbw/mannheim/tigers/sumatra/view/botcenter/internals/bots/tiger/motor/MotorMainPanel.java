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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams.MotorMode;

/**
 * Motor main panel with a summary.
 * 
 * @author AndreR
 * 
 */
public class MotorMainPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 4849530945825008640L;
	
	private JTextField xSpeed = null;
	private JTextField ySpeed = null;
	private JTextField wSpeed = null;
	private JTextField vSpeed = null;
	private MotorEnhancedInputPanel enhanced = null;
	private JComboBox mode = null;
	
	private final List<IMotorMainPanelObserver> observers = new ArrayList<IMotorMainPanelObserver>();
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public MotorMainPanel()
	{
		setLayout(new MigLayout("", "[]20[]", ""));
		
		xSpeed = new JTextField();
		ySpeed = new JTextField();
		wSpeed = new JTextField();
		vSpeed = new JTextField();
		enhanced = new MotorEnhancedInputPanel();
		
		JButton setSpeed = new JButton("Set Speed");
		setSpeed.addActionListener(new SetSpeed());
		
		JPanel leftPanel = new JPanel(new MigLayout("wrap 2", "[100]10[100,fill]", ""));
		
		leftPanel.add(new JLabel("X Speed:"));
		leftPanel.add(xSpeed);
		leftPanel.add(new JLabel("Y Speed:"));
		leftPanel.add(ySpeed);
		leftPanel.add(new JLabel("W Speed:"));
		leftPanel.add(wSpeed);
		leftPanel.add(new JLabel("V Speed:"));
		leftPanel.add(vSpeed);
		leftPanel.add(setSpeed, "span 2");
		leftPanel.add(enhanced, "span 2");
		
		String modes[] = {"Manual", "PID", "Automatic"};

		mode = new JComboBox(modes);
		
		JPanel modePanel = new JPanel(new MigLayout("", "[100,fill]20[50,fill]", ""));
		modePanel.setBorder(BorderFactory.createTitledBorder("Motor Mode"));
		
		modePanel.add(mode);
		
		JButton saveMode = new JButton("Set");
		saveMode.addActionListener(new SaveMotorMode());
		
		modePanel.add(saveMode);
		
		JPanel rightPanel = new JPanel(new MigLayout("", "", ""));
		rightPanel.add(modePanel, "aligny top, wrap");
		
		add(leftPanel);
		add(rightPanel, "grow");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IMotorMainPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.add(observer);
		}
	}
	
	
	public void removeObserver(IMotorMainPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.remove(observer);
		}
	}
	
	private void notifySetAutomaticSpeed(float x, float y, float w, float v)
	{
		synchronized(observers)
		{
			for (IMotorMainPanelObserver observer : observers)
			{
				observer.onSetAutomaticSpeed(x, y, w, v);
			}
		}
	}
		
	private void notifySetMotorMode(MotorMode mode)
	{
		synchronized(observers)
		{
			for (IMotorMainPanelObserver observer : observers)
			{
				observer.onSetMotorMode(mode);
			}
		}
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public MotorEnhancedInputPanel getEnhancedInputPanel()
	{
		return enhanced;
	}
	
	public void setMode(final MotorMode m)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				mode.setSelectedIndex(m.ordinal());
			}
		});
	}
		
	private class SetSpeed implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float x;
			float y;
			float w;
			float v;
			
			try
			{
				x = Float.valueOf(xSpeed.getText());
				y = Float.valueOf(ySpeed.getText());
				w = Float.valueOf(wSpeed.getText());
				v = Float.valueOf(vSpeed.getText());
			}
			
			catch(NumberFormatException ex)
			{
				return;
			}
			
			notifySetAutomaticSpeed(x, y, w, v);
		}
	}
	
	private class SaveMotorMode implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifySetMotorMode(MotorMode.values()[mode.getSelectedIndex()]);
		}
	}
}
