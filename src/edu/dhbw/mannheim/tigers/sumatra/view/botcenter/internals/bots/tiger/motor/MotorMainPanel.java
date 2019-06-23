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
import javax.swing.JPanel;
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
	private static final long							serialVersionUID	= 4849530945825008640L;
	
	private MotorInputPanel								input					= null;
	private MotorEnhancedInputPanel					enhanced				= null;
	private JComboBox<MotorMode>						mode					= null;
	
	private final List<IMotorMainPanelObserver>	observers			= new ArrayList<IMotorMainPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public MotorMainPanel()
	{
		setLayout(new MigLayout("", "[]20[]", ""));
		
		enhanced = new MotorEnhancedInputPanel();
		
		input = new MotorInputPanel(true);
		
		mode = new JComboBox<MotorMode>(MotorMode.values());
		
		final JPanel leftPanel = new JPanel(new MigLayout("", "", ""));
		leftPanel.add(input, "wrap");
		leftPanel.add(enhanced);
		
		final JPanel modePanel = new JPanel(new MigLayout("", "[100,fill]20[50,fill]", ""));
		modePanel.setBorder(BorderFactory.createTitledBorder("Motor Mode"));
		
		modePanel.add(mode);
		
		final JButton saveMode = new JButton("Set");
		saveMode.addActionListener(new SaveMotorMode());
		
		modePanel.add(saveMode);
		
		final JPanel rightPanel = new JPanel(new MigLayout("", "", ""));
		rightPanel.add(modePanel, "aligny top, wrap");
		
		add(leftPanel);
		add(rightPanel, "grow");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IMotorMainPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IMotorMainPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifySetMotorMode(MotorMode mode)
	{
		synchronized (observers)
		{
			for (final IMotorMainPanelObserver observer : observers)
			{
				observer.onSetMotorMode(mode);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public MotorEnhancedInputPanel getEnhancedInputPanel()
	{
		return enhanced;
	}
	
	
	/**
	 * @return
	 */
	public MotorInputPanel getInputPanel()
	{
		return input;
	}
	
	
	/**
	 * @param m
	 */
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
	
	
	private class SaveMotorMode implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifySetMotorMode((MotorMode) mode.getSelectedItem());
		}
	}
}
