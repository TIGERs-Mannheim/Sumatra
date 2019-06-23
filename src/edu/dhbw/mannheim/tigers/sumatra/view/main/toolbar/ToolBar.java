/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;


/**
 * The frame tool bar.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ToolBar
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private JToolBar								toolBar;
	
	private final List<IToolbarObserver>	observers				= new ArrayList<IToolbarObserver>();
	
	// --- toolbar ---
	private JButton								startStopButton		= null;
	private JButton								emergencyStopButton	= null;
	private FpsPanel								fpsPanel					= null;
	
	
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
		startStopButton.setBorder(null);
		startStopButton.setEnabled(false);
		
		emergencyStopButton = new JButton();
		emergencyStopButton.setForeground(Color.red);
		emergencyStopButton.addActionListener(new EmergencyStopListener());
		emergencyStopButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("stop-emergency.png")));
		emergencyStopButton.setToolTipText("Emergency stop");
		emergencyStopButton.setEnabled(false);
		
		fpsPanel = new FpsPanel();
		
		// --- configure toolbar ---
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		// --- add buttons ---
		toolBar.addSeparator();
		toolBar.add(startStopButton);
		toolBar.addSeparator();
		toolBar.add(emergencyStopButton);
		toolBar.addSeparator();
		toolBar.add(fpsPanel, "fill");
		toolBar.addSeparator();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addObserver(IToolbarObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IToolbarObserver o)
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
	 * @param enable
	 */
	public void setStartStopButtonState(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				startStopButton.setEnabled(enable);
			}
		});
	}
	
	
	/**
	 * @param enable
	 * @param icon
	 */
	public void setStartStopButtonState(final boolean enable, final ImageIcon icon)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				startStopButton.setEnabled(enable);
				startStopButton.setIcon(icon);
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
		public void actionPerformed(ActionEvent e)
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
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IToolbarObserver o : observers)
				{
					o.onStartStopModules();
				}
			}
		}
	}
}
