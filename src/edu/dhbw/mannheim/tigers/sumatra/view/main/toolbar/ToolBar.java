/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.util.ImageScaler;


/**
 * The frame tool bar.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ToolBar
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger				log						= Logger.getLogger(ToolBar.class.getName());
	
	private final List<IToolbarObserver>	observers				= new ArrayList<IToolbarObserver>();
	
	// --- toolbar ---
	private JToolBar								toolBar;
	private JButton								startStopButton		= null;
	private JButton								emergencyStopButton	= null;
	private FpsPanel								fpsPanel					= null;
	private InformationPanel					informationPanel		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ToolBar()
	{
		// --- configure buttons ---
		startStopButton = new JButton();
		startStopButton.addActionListener(new StartStopModules());
		startStopButton.setBorder(BorderFactory.createEmptyBorder());
		
		emergencyStopButton = new JButton();
		emergencyStopButton.setForeground(Color.red);
		emergencyStopButton.addActionListener(new EmergencyStopListener());
		emergencyStopButton.setIcon(ImageScaler.scaleDefaultButtonImageIcon("stop-emergency.png"));
		emergencyStopButton.setToolTipText("Emergency stop");
		emergencyStopButton.setEnabled(false);
		emergencyStopButton.setBorder(BorderFactory.createEmptyBorder());
		emergencyStopButton.setToolTipText("Escape");
		
		fpsPanel = new FpsPanel();
		informationPanel = new InformationPanel();
		
		// --- configure toolbar ---
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new MigLayout("inset 1"));
		
		// --- add buttons ---
		toolBarPanel.add(startStopButton, "left");
		toolBarPanel.add(emergencyStopButton, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(informationPanel, "left");
		toolBar.add(toolBarPanel);
		
		// initialize icons
		log.trace("Loaded button icon " + EStartStopButtonState.LOADING.name());
		log.trace("Loaded button icon " + EStartStopButtonState.START.name());
		log.trace("Loaded button icon " + EStartStopButtonState.STOP.name());
		
		GlobalShortcuts.register(EShortcut.EMERGENCY_MODE, new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (observers)
				{
					for (final IToolbarObserver o : observers)
					{
						o.onEmergencyStop();
					}
				}
			}
		});
		GlobalShortcuts.register(EShortcut.START_STOP, new Runnable()
		{
			@Override
			public void run()
			{
				startStopModules();
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addObserver(final IToolbarObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IToolbarObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public JToolBar getToolbar()
	{
		return toolBar;
	}
	
	
	/**
	 * @return the fpsPanel
	 */
	public FpsPanel getFpsPanel()
	{
		return fpsPanel;
	}
	
	
	/**
	 * @return
	 */
	public InformationPanel getInformationPanel()
	{
		return informationPanel;
	}
	
	
	/**
	 * @param enable
	 * @param state
	 */
	public void setStartStopButtonState(final boolean enable, final EStartStopButtonState state)
	
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				startStopButton.setEnabled(enable);
				startStopButton.setIcon(state.getIcon());
				switch (state)
				{
					case LOADING:
						startStopButton.setDisabledIcon(state.getIcon());
						break;
					case START:
					case STOP:
						startStopButton.setDisabledIcon(null);
						break;
					default:
						break;
				
				}
			}
		});
	}
	
	
	/**
	 * @param enabled
	 */
	public void setEmergencyStopButtonEnabled(final boolean enabled)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				emergencyStopButton.setEnabled(enabled);
			}
		});
	}
	
	private class EmergencyStopListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IToolbarObserver o : observers)
				{
					o.onEmergencyStop();
				}
			}
		}
	}
	
	protected class StartStopModules implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			startStopModules();
		}
	}
	
	
	private void startStopModules()
	{
		synchronized (observers)
		{
			for (final IToolbarObserver o : observers)
			{
				informationPanel.clearView();
				o.onStartStopModules();
			}
		}
	}
}
