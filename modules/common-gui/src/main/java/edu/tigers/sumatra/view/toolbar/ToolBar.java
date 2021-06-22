/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.toolbar;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.view.FpsPanel;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The frame tool bar.
 */
@Log4j2
public class ToolBar
{
	private final List<IToolbarObserver> observers = new CopyOnWriteArrayList<>();

	private final JToolBar jToolBar;

	private final JButton btnRecSave;


	private final FpsPanel fpsPanel = new FpsPanel();
	private final JProgressBar heapBar = new JProgressBar();
	private final JLabel heapLabel = new JLabel();


	/**
	 * The toolbar
	 */
	public ToolBar()
	{
		log.trace("Create toolbar");

		var btnEmergency = new JButton();
		btnEmergency.setForeground(Color.red);
		btnEmergency.addActionListener(new EmergencyStopListener());
		btnEmergency.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/stop-emergency.png"));
		btnEmergency.setToolTipText("Emergency stop [Esc]");
		btnEmergency.setBorder(BorderFactory.createEmptyBorder());
		btnEmergency.setBackground(new Color(0, 0, 0, 1));

		btnRecSave = new JButton();
		btnRecSave.addActionListener(new RecordSaveButtonListener());
		btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/record.png"));
		btnRecSave.setToolTipText("Start/Stop recording");
		btnRecSave.setBorder(BorderFactory.createEmptyBorder());
		btnRecSave.setBackground(new Color(0, 0, 0, 1));

		var btnTournament = new JToggleButton();
		btnTournament.addActionListener(new TournamentListener());
		btnTournament.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/tournament_bw.png"));
		btnTournament.setToolTipText("Tournament mode (off)");
		btnTournament.setBorder(BorderFactory.createEmptyBorder());
		btnTournament.setBackground(new Color(0, 0, 0, 1));
		btnTournament.setContentAreaFilled(false);

		JPanel heapPanel = new JPanel(new BorderLayout());
		heapLabel.setToolTipText("Memory Usage (current/total/maximum)");
		heapPanel.add(heapLabel, BorderLayout.NORTH);
		heapPanel.add(heapBar, BorderLayout.SOUTH);
		heapBar.setStringPainted(true);
		heapBar.setMinimum(0);
		heapBar.setToolTipText("Memory Usage");

		// --- configure toolbar ---
		jToolBar = new JToolBar();
		jToolBar.setFloatable(false);
		jToolBar.setRollover(true);

		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new MigLayout("inset 1"));

		// --- add buttons ---
		toolBarPanel.add(btnEmergency, "left");
		toolBarPanel.add(btnRecSave, "left");
		toolBarPanel.add(btnTournament, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(heapPanel, "left");
		jToolBar.add(toolBarPanel);

		// initialize icons
		for (EStartStopButtonState icon : EStartStopButtonState.values())
		{
			log.trace("Load button icon " + icon.name());
		}

		GlobalShortcuts.register(EShortcut.EMERGENCY_MODE, () -> {
			for (final IToolbarObserver o : observers)
			{
				o.onEmergencyStop();
			}
		});
	}


	/**
	 * @param o
	 */
	public void addObserver(final IToolbarObserver o)
	{
		observers.add(o);
	}


	/**
	 * @param o
	 */
	public void removeObserver(final IToolbarObserver o)
	{
		observers.remove(o);
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------


	/**
	 * @return
	 */
	public JToolBar getToolbar()
	{
		return jToolBar;
	}


	/**
	 * @return the fpsPanel
	 */
	public FpsPanel getFpsPanel()
	{
		return fpsPanel;
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

		jToolBar.repaint();
	}


	private class EmergencyStopListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IToolbarObserver o : observers)
			{
				o.onEmergencyStop();
			}
			SwingUtilities.invokeLater(jToolBar::repaint);
		}
	}

	private class TournamentListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JToggleButton btn = (JToggleButton) e.getSource();
			SumatraModel.getInstance().setProductive(btn.isSelected());
			if (btn.isSelected())
			{
				btn.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/tournament_color.png"));
				btn.setToolTipText("Tournament mode (on)");
			} else
			{
				btn.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/tournament_bw.png"));
				btn.setToolTipText("Tournament mode (off)");
			}
			SwingUtilities.invokeLater(jToolBar::repaint);
		}
	}


	private class RecordSaveButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			Thread t = new Thread(() -> {
				btnRecSave.setEnabled(false);
				for (IToolbarObserver observer : observers)
				{
					observer.onToggleRecord();
				}
				btnRecSave.setEnabled(true);
			}, "RecordSaveButton");

			t.start();
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
