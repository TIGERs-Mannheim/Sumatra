/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.view.toolbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.view.FpsPanel;


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
	private static final Logger				log			= Logger.getLogger(ToolBar.class.getName());
	
	private final List<IToolbarObserver>	observers	= new ArrayList<IToolbarObserver>();
	
	// --- toolbar ---
	private final JToolBar						toolBar;
	
	private final JButton						btnStartStop;
	private final JButton						btnEmergency;
	private final JButton						btnRecSave;
	
	private final FpsPanel						fpsPanel		= new FpsPanel();
	private final JProgressBar					heapBar		= new JProgressBar();
	private final JLabel							heapLabel	= new JLabel();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ToolBar()
	{
		log.trace("Create toolbar");
		// --- configure buttons ---
		btnStartStop = new JButton();
		btnStartStop.addActionListener(new StartStopModules());
		btnStartStop.setBorder(BorderFactory.createEmptyBorder());
		btnStartStop.setBackground(new Color(0, 0, 0, 1));
		
		btnEmergency = new JButton();
		btnEmergency.setForeground(Color.red);
		btnEmergency.addActionListener(new EmergencyStopListener());
		btnEmergency.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/stop-emergency.png"));
		btnEmergency.setToolTipText("Emergency stop [Esc]");
		btnEmergency.setEnabled(false);
		btnEmergency.setBorder(BorderFactory.createEmptyBorder());
		btnEmergency.setBackground(new Color(0, 0, 0, 1));
		
		btnRecSave = new JButton();
		btnRecSave.addActionListener(new RecordSaveButtonListener());
		btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/record.png"));
		btnRecSave.setToolTipText("Start/Stop recording");
		btnRecSave.setEnabled(false);
		btnRecSave.setBorder(BorderFactory.createEmptyBorder());
		btnRecSave.setBackground(new Color(0, 0, 0, 1));
		
		JPanel heapPanel = new JPanel(new BorderLayout());
		heapPanel.add(heapLabel, BorderLayout.NORTH);
		heapPanel.add(heapBar, BorderLayout.SOUTH);
		heapBar.setStringPainted(true);
		heapBar.setMinimum(0);
		
		// --- configure toolbar ---
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new MigLayout("inset 1"));
		
		// --- add buttons ---
		toolBarPanel.add(btnStartStop, "left");
		toolBarPanel.add(btnEmergency, "left");
		toolBarPanel.add(btnRecSave, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(heapPanel, "left");
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
				btnStartStop.setEnabled(enable);
				btnStartStop.setIcon(state.getIcon());
				switch (state)
				{
					case LOADING:
						btnStartStop.setDisabledIcon(state.getIcon());
						break;
					case START:
					case STOP:
						btnStartStop.setDisabledIcon(null);
						break;
					default:
						break;
				}
				toolBar.repaint();
			}
		});
	}
	
	
	/**
	 * @param enabled
	 */
	public void setActive(final boolean enabled)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				btnEmergency.setEnabled(enabled);
				btnRecSave.setEnabled(enabled);
			}
		});
	}
	
	
	private void startStopModules()
	{
		synchronized (observers)
		{
			for (final IToolbarObserver o : observers)
			{
				o.onStartStopModules();
			}
		}
	}
	
	
	/**
	 * @param recording
	 */
	public void setRecordingEnabled(final boolean recording)
	{
		if (recording)
		{
			btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/recordActive.gif"));
		} else
		{
			btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/record.png"));
		}
		
		toolBar.repaint();
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
	
	private class StartStopModules implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			startStopModules();
		}
	}
	
	
	private class RecordSaveButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IToolbarObserver observer : observers)
			{
				observer.onToggleRecord();
			}
		}
	}
	
	
	/**
	 * @return the heapBar
	 */
	public final JProgressBar getHeapBar()
	{
		return heapBar;
	}
	
	
	/**
	 * @return the heapLabel
	 */
	public final JLabel getHeapLabel()
	{
		return heapLabel;
	}
}
