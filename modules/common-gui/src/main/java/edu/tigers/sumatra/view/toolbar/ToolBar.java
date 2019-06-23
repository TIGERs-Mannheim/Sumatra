/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.view.FpsPanel;
import net.miginfocom.swing.MigLayout;


/**
 * The frame tool bar.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ToolBar
{
	private static final Logger log = Logger.getLogger(ToolBar.class.getName());
	
	private final List<IToolbarObserver> observers = new ArrayList<>();
	
	// --- toolbar ---
	private final JToolBar toolBar;
	
	private final JButton btnStartStop;
	private final JButton btnEmergency;
	private final JButton btnRecSave;
	private final JButton btnSwitchSides;
	
	
	private JCheckBox telegramMode = new JCheckBox("Telegram");
	private final FpsPanel fpsPanel = new FpsPanel();
	private final JProgressBar heapBar = new JProgressBar();
	private final JLabel heapLabel = new JLabel();
	
	
	/**
	 * The toolbar
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
		
		btnSwitchSides = new JButton();
		btnSwitchSides.addActionListener(new SwitchSidesButtonListener());
		btnSwitchSides.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/switch.png"));
		btnSwitchSides.setToolTipText("Switch sides");
		btnSwitchSides.setEnabled(false);
		btnSwitchSides.setBorder(BorderFactory.createEmptyBorder());
		btnSwitchSides.setBackground(new Color(0, 0, 0, 1));
		
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
		
		JPanel matchModePanel = new JPanel(new BorderLayout());
		telegramMode.addActionListener(new ChangeMatchModeListener());
		matchModePanel.add(telegramMode);
		
		// --- add buttons ---
		toolBarPanel.add(btnStartStop, "left");
		toolBarPanel.add(btnEmergency, "left");
		toolBarPanel.add(btnRecSave, "left");
		toolBarPanel.add(btnSwitchSides, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(heapPanel, "left");
		toolBarPanel.add(matchModePanel, "right");
		toolBar.add(toolBarPanel);
		
		// initialize icons
		for (EStartStopButtonState icon : EStartStopButtonState.values())
		{
			log.trace("Load button icon " + icon.name());
		}
		
		GlobalShortcuts.register(EShortcut.EMERGENCY_MODE, () -> {
			synchronized (observers)
			{
				for (final IToolbarObserver o : observers)
				{
					o.onEmergencyStop();
				}
			}
		});
		GlobalShortcuts.register(EShortcut.START_STOP, this::startStopModules);
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
		SwingUtilities.invokeLater(() -> {
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
		});
	}
	
	
	/**
	 * Sets the current telegram mode
	 * 
	 * @param enabled
	 */
	public void setTelegramStatus(boolean enabled)
	{
		telegramMode.setSelected(enabled);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setActive(final boolean enabled)
	{
		SwingUtilities.invokeLater(() -> {
			btnEmergency.setEnabled(enabled);
			btnRecSave.setEnabled(enabled);
			btnSwitchSides.setEnabled(enabled);
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
	
	
	private class SwitchSidesButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IToolbarObserver observer : observers)
			{
				observer.onSwitchSides();
			}
		}
	}
	
	private class ChangeMatchModeListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			for (IToolbarObserver observer : observers)
			{
				observer.onChangeTelegramMode(telegramMode.isSelected());
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
