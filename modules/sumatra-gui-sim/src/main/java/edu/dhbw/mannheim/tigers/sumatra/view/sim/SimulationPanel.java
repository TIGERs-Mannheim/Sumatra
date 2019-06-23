/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.view.sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPanel extends JPanel implements ISumatraView
{
	/**  */
	private static final long serialVersionUID = 4936408016928626573L;
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(SimulationPanel.class.getName());
	
	private final JToggleButton btnPauseSim;
	private final JSlider sliderSpeed;
	private final JLabel labelSpeed;
	private final JButton btnLoadSnapshot;
	private final JFileChooser fcOpenSnapshot;
	private final JLabel lblTime;
	private final JCheckBox chkSyncWithAi;
	
	private boolean paused = false;
	
	private final transient List<ISimulationPanelObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * Default
	 */
	public SimulationPanel()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		Action pauseSimulationAction = new PauseSimulationAction();
		btnPauseSim = new JToggleButton();
		btnPauseSim.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/pause.png"));
		btnPauseSim.setSelectedIcon(ImageScaler.scaleDefaultButtonImageIcon("/play.png"));
		btnPauseSim.setBorder(BorderFactory.createEmptyBorder());
		btnPauseSim.setBackground(new Color(0, 0, 0, 0));
		btnPauseSim.setToolTipText("Pause/Play");
		btnPauseSim.setActionCommand(PauseSimulationAction.class.getCanonicalName());
		btnPauseSim.addActionListener(pauseSimulationAction);
		registerShortcut(btnPauseSim, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK),
				pauseSimulationAction);
		
		final JButton btnStepBwd = new JButton();
		btnStepBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameBackward.png"));
		btnStepBwd.setBorder(BorderFactory.createEmptyBorder());
		btnStepBwd.setBackground(new Color(0, 0, 0, 1));
		Action stepBackAction = new StepBwdAction();
		btnStepBwd.addActionListener(stepBackAction);
		btnStepBwd.setToolTipText("Step backward");
		registerShortcut(btnStepBwd, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), stepBackAction);
		
		final JButton btnStep = new JButton();
		btnStep.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameForward.png"));
		btnStep.setBorder(BorderFactory.createEmptyBorder());
		btnStep.setBackground(new Color(0, 0, 0, 1));
		Action stepAction = new StepAction();
		btnStep.addActionListener(stepAction);
		btnStep.setToolTipText("Step forward");
		registerShortcut(btnStep, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), stepAction);
		
		sliderSpeed = new JSlider(-9, 9, 0);
		sliderSpeed.setMajorTickSpacing(5);
		sliderSpeed.setMinorTickSpacing(1);
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setPaintTicks(true);
		sliderSpeed.addChangeListener(new SpeedListener());
		
		labelSpeed = new JLabel("x1");
		labelSpeed.setPreferredSize(new Dimension(30, labelSpeed.getMaximumSize().height));
		
		chkSyncWithAi = new JCheckBox("Sync", false);
		chkSyncWithAi.setToolTipText("Sync speed with AI such that AI can process each WF.");
		chkSyncWithAi.addActionListener(new SyncWithAiAction());
		
		lblTime = new JLabel("0");
		lblTime.setPreferredSize(new Dimension(100, lblTime.getMaximumSize().height));
		
		btnLoadSnapshot = new JButton();
		btnLoadSnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/open.png"));
		btnLoadSnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnLoadSnapshot.setBackground(new Color(0, 0, 0, 1));
		btnLoadSnapshot.setToolTipText("Open from file");
		Action loadAction = new OpenSnapAction();
		btnLoadSnapshot.addActionListener(loadAction);
		registerShortcut(btnLoadSnapshot, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), loadAction);
		
		final JButton btnSaveSnapshot = new JButton();
		btnSaveSnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/save.png"));
		btnSaveSnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnSaveSnapshot.setBackground(new Color(0, 0, 0, 1));
		btnSaveSnapshot.setToolTipText("Save to file");
		Action saveAction = new SaveSnapAction();
		btnSaveSnapshot.addActionListener(saveAction);
		registerShortcut(btnSaveSnapshot, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), saveAction);
		
		final JButton btnCopySnapshot = new JButton();
		btnCopySnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/copy.png"));
		btnCopySnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnCopySnapshot.setBackground(new Color(0, 0, 0, 1));
		btnCopySnapshot.setToolTipText("Copy to clipboard");
		Action copyAction = new CopySnapAction();
		btnCopySnapshot.addActionListener(copyAction);
		registerShortcut(btnCopySnapshot, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), copyAction);
		
		final JButton btnPasteSnapshot = new JButton();
		btnPasteSnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/paste.png"));
		btnPasteSnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnPasteSnapshot.setBackground(new Color(0, 0, 0, 1));
		btnPasteSnapshot.setToolTipText("Paste from clipboard");
		Action pasteAction = new PasteSnapAction();
		btnPasteSnapshot.addActionListener(pasteAction);
		registerShortcut(btnPasteSnapshot, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), pasteAction);
		
		
		add(btnPauseSim);
		add(btnStepBwd);
		add(btnStep);
		add(btnLoadSnapshot);
		add(btnSaveSnapshot);
		add(btnCopySnapshot);
		add(btnPasteSnapshot);
		add(sliderSpeed);
		add(labelSpeed);
		add(chkSyncWithAi);
		add(lblTime);
		
		String path = null;
		try
		{
			path = Paths.get("").toFile().getCanonicalPath() + "/data/snapshots";
			if (new File(path).mkdirs())
			{
				log.info("New directory created: " + path);
			}
		} catch (IOException e)
		{
			log.error("", e);
		}
		
		fcOpenSnapshot = new JFileChooser(path);
	}
	
	
	private void registerShortcut(JComponent component, KeyStroke keyStroke, Action action)
	{
		String actionCommand = action.getClass().getCanonicalName();
		component.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionCommand);
		component.getActionMap().put(actionCommand, action);
	}
	
	
	/**
	 * Update the time label
	 * 
	 * @param timestamp in [ns]
	 */
	public void updateTime(final long timestamp)
	{
		long timestampMs = (long) (timestamp / 1e6);
		Date date = new Date(timestampMs);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
		String txt = sdf.format(date);
		EventQueue.invokeLater(() -> lblTime.setText(txt));
	}
	
	
	/**
	 * Reset gui
	 */
	public void reset()
	{
		EventQueue.invokeLater(() -> {
			sliderSpeed.setValue(0);
			btnPauseSim.setSelected(false);
		});
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISimulationPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISimulationPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return new ArrayList<>();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public interface ISimulationPanelObserver
	{
		/**
		 * @param params
		 */
		void onRunSimulation(SimulationParameters params);
		
		
		/**
		 * Pause
		 */
		void onPauseSimulation();
		
		
		/**
		 * Resume
		 */
		void onResumeSimulation();
		
		
		/**
		 * @param speed
		 */
		void onChangeSpeed(double speed);
		
		
		/**
		 * @param i
		 */
		void onStep(int i);
		
		
		/**
		 * @param i
		 */
		void onStepBwd(int i);
		
		
		/**
		 * Reset
		 */
		void onReset();
		
		
		/**
		 * @param path
		 */
		void onLoadSnapshot(String path);
		
		
		/**
		 * Save current situation as snapshot
		 */
		void onSaveSnapshot();
		
		
		/**
		 * Sync speed with AI threads.
		 * 
		 * @param sync
		 */
		void onSyncWithAi(boolean sync);
		
		
		/**
		 * Copy snapshot to clipboard
		 */
		void onCopySnapshot();
		
		
		/**
		 * Paste snapshot from clipboard
		 */
		void onPasteSnapshot();
	}
	
	private class PauseSimulationAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (paused)
			{
				for (ISimulationPanelObserver o : observers)
				{
					o.onResumeSimulation();
				}
				btnPauseSim.setSelected(false);
			} else
			{
				for (ISimulationPanelObserver o : observers)
				{
					o.onPauseSimulation();
				}
				btnPauseSim.setSelected(true);
			}
			paused = !paused;
		}
	}
	
	private class SpeedListener implements ChangeListener
	{
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			int speed = sliderSpeed.getValue();
			final double simSpeed;
			
			if (speed < 0)
			{
				labelSpeed.setText(String.format("x1/%d", -(speed - 1)));
				simSpeed = 1.0 / -(speed - 1);
			} else if (speed == 0)
			{
				labelSpeed.setText("x1");
				simSpeed = 1;
			} else
			{
				labelSpeed.setText(String.format("x%d", speed + 1));
				simSpeed = speed + 1.0;
			}
			for (ISimulationPanelObserver o : observers)
			{
				o.onChangeSpeed(simSpeed);
			}
		}
	}
	
	private class StepAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onStep(1);
			}
		}
	}
	
	private class StepBwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onStepBwd(1);
			}
		}
	}
	
	private class SaveSnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onSaveSnapshot();
			}
		}
	}
	
	private class CopySnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onCopySnapshot();
			}
		}
	}
	
	private class PasteSnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onPasteSnapshot();
			}
		}
	}
	
	
	private class OpenSnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int returnVal = fcOpenSnapshot.showOpenDialog(btnLoadSnapshot);
			
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fcOpenSnapshot.getSelectedFile();
				
				for (ISimulationPanelObserver o : observers)
				{
					try
					{
						o.onLoadSnapshot(file.getCanonicalPath());
					} catch (IOException e1)
					{
						log.error("", e1);
					}
				}
			}
		}
	}
	
	private class SyncWithAiAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onSyncWithAi(chkSyncWithAi.isSelected());
			}
		}
	}
}
