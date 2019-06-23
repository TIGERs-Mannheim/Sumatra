/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar;

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
import javax.swing.JToggleButton;
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
	private static final Logger				log					= Logger.getLogger(ToolBar.class.getName());
	
	private final List<IToolbarObserver>	observers			= new ArrayList<IToolbarObserver>();
	
	// --- toolbar ---
	private JToolBar								toolBar;
	
	private final JButton						btnStartStop;
	private final JButton						btnEmergency;
	private final JToggleButton				btnRec;
	private final JToggleButton				btnRecSave;
	
	private final FpsPanel						fpsPanel				= new FpsPanel();
	private final InformationPanel			informationPanel	= new InformationPanel();
	private final JProgressBar					heapBar				= new JProgressBar();
	private final JLabel							heapLabel			= new JLabel();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ToolBar()
	{
		// --- configure buttons ---
		btnStartStop = new JButton();
		btnStartStop.addActionListener(new StartStopModules());
		btnStartStop.setBorder(BorderFactory.createEmptyBorder());
		
		btnEmergency = new JButton();
		btnEmergency.setForeground(Color.red);
		btnEmergency.addActionListener(new EmergencyStopListener());
		btnEmergency.setIcon(ImageScaler.scaleDefaultButtonImageIcon("stop-emergency.png"));
		btnEmergency.setToolTipText("Emergency stop [Esc]");
		btnEmergency.setEnabled(false);
		btnEmergency.setBorder(BorderFactory.createEmptyBorder());
		
		btnRec = new JToggleButton();
		btnRec.addActionListener(new RecordButtonListener());
		btnRec.setIcon(ImageScaler.scaleDefaultButtonImageIcon("record.png"));
		btnRec.setToolTipText("Record without saving");
		btnRec.setEnabled(false);
		btnRec.setBorder(BorderFactory.createEmptyBorder());
		
		btnRecSave = new JToggleButton();
		btnRecSave.addActionListener(new RecordSaveButtonListener());
		btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("record_save.png"));
		btnRecSave.setToolTipText("Record and save");
		btnRecSave.setEnabled(false);
		btnRecSave.setBorder(BorderFactory.createEmptyBorder());
		
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
		toolBarPanel.add(btnRec, "left");
		toolBarPanel.add(btnRecSave, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(informationPanel, "left");
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
				btnRec.setEnabled(enabled);
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
				informationPanel.clearView();
				o.onStartStopModules();
			}
		}
	}
	
	
	/**
	 * @param recording
	 * @param persisting
	 */
	public void setRecordingEnabled(final boolean recording, final boolean persisting)
	{
		if (persisting)
		{
			btnRec.setEnabled(!recording);
			btnRecSave.setEnabled(true);
			btnRecSave.setSelected(recording);
			btnRec.setSelected(false);
		} else
		{
			btnRecSave.setEnabled(!recording);
			btnRec.setEnabled(true);
			btnRec.setSelected(recording);
			btnRecSave.setSelected(false);
		}
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
	
	
	private class RecordButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IToolbarObserver observer : observers)
			{
				observer.onRecord(btnRec.isSelected());
			}
			if (btnRec.isSelected())
			{
				btnRecSave.setEnabled(false);
			} else
			{
				btnRecSave.setEnabled(true);
			}
		}
	}
	
	private class RecordSaveButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IToolbarObserver observer : observers)
			{
				observer.onRecordAndSave(btnRecSave.isSelected());
			}
			if (btnRecSave.isSelected())
			{
				btnRec.setEnabled(false);
			} else
			{
				btnRec.setEnabled(true);
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
