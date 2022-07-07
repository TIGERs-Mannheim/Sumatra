/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.sim;

import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.util.ScalingUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Main panel for simulation view.
 */
@Log4j2
public class SimulationPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 4936408016928626573L;

	@Getter
	private final JToggleButton btnToggleSim;
	@Getter
	private final JSlider sliderSpeed;
	private final JLabel lblSpeed;
	private final JLabel lblTime;
	private final JLabel lblRelativeTime;
	@Getter
	private final JToggleButton btnSlowmotion;

	private final SimulationBotMgrPanel botMgrPanel;

	private final transient List<ISimulationPanelObserver> observers = new CopyOnWriteArrayList<>();


	/**
	 * Default
	 */
	public SimulationPanel()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));

		Action pauseSimulationAction = new PauseSimulationAction();
		btnToggleSim = new JToggleButton();
		btnToggleSim.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/pause.png"));
		btnToggleSim.setSelectedIcon(ImageScaler.scaleDefaultButtonImageIcon("/play.png"));
		btnToggleSim.setBorder(BorderFactory.createEmptyBorder());
		btnToggleSim.setBackground(new Color(0, 0, 0, 0));
		btnToggleSim.setToolTipText("Pause/Play");
		btnToggleSim.addActionListener(pauseSimulationAction);

		final JButton btnStepBwd = new JButton();
		btnStepBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameBackward.png"));
		btnStepBwd.setBorder(BorderFactory.createEmptyBorder());
		btnStepBwd.setBackground(new Color(0, 0, 0, 1));
		Action stepBackAction = new StepBwdAction();
		btnStepBwd.addActionListener(stepBackAction);
		btnStepBwd.setToolTipText("Step backward");

		final JButton btnStep = new JButton();
		btnStep.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameForward.png"));
		btnStep.setBorder(BorderFactory.createEmptyBorder());
		btnStep.setBackground(new Color(0, 0, 0, 1));
		Action stepAction = new StepAction();
		btnStep.addActionListener(stepAction);
		btnStep.setToolTipText("Step forward");

		final JButton btnReset = new JButton();
		btnReset.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/refresh.png"));
		btnReset.setBorder(BorderFactory.createEmptyBorder());
		btnReset.setBackground(new Color(0, 0, 0, 1));
		Action resetAction = new ResetAction();
		btnReset.addActionListener(resetAction);
		btnReset.setToolTipText("Reset simulation");

		btnSlowmotion = new JToggleButton();
		btnSlowmotion.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/slowMotion.png"));
		btnSlowmotion.setBorder(BorderFactory.createEmptyBorder());
		btnSlowmotion.setBackground(new Color(0, 0, 0, 1));
		btnSlowmotion.setToolTipText("Toggle slow motion");
		btnSlowmotion.addActionListener(e -> observers.forEach(ISimulationPanelObserver::onToggleSlowMotion));

		sliderSpeed = new JSlider(-9, 9, 0);
		sliderSpeed.setMajorTickSpacing(5);
		sliderSpeed.setMinorTickSpacing(1);
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setPaintTicks(true);
		sliderSpeed.addChangeListener(new SpeedListener());

		lblSpeed = new JLabel("x1");
		lblSpeed.setPreferredSize(new Dimension((int) (ScalingUtil.getFontSize(EFontSize.MEDIUM) * 3.0),
				lblSpeed.getMaximumSize().height));

		lblRelativeTime = new JLabel("(x1)");
		lblRelativeTime.setPreferredSize(
				new Dimension((int) (ScalingUtil.getFontSize(EFontSize.MEDIUM) * 4.0),
						lblRelativeTime.getMaximumSize().height));

		lblTime = new JLabel("-");
		lblTime.setPreferredSize(new Dimension((int) (ScalingUtil.getFontSize(EFontSize.MEDIUM) * 8.0),
				lblTime.getMaximumSize().height));
		var btnLoadSnapshot = new JButton();
		btnLoadSnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/open.png"));
		btnLoadSnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnLoadSnapshot.setBackground(new Color(0, 0, 0, 1));
		btnLoadSnapshot.setToolTipText("Open from file");
		Action loadAction = new OpenSnapAction();
		btnLoadSnapshot.addActionListener(loadAction);

		final JButton btnSaveSnapshot = new JButton();
		btnSaveSnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/save.png"));
		btnSaveSnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnSaveSnapshot.setBackground(new Color(0, 0, 0, 1));
		btnSaveSnapshot.setToolTipText("Save to file");
		Action saveAction = new SaveSnapAction();
		btnSaveSnapshot.addActionListener(saveAction);

		final JButton btnCopySnapshot = new JButton();
		btnCopySnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/copy.png"));
		btnCopySnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnCopySnapshot.setBackground(new Color(0, 0, 0, 1));
		btnCopySnapshot.setToolTipText("Copy to clipboard");
		Action copyAction = new CopySnapAction();
		btnCopySnapshot.addActionListener(copyAction);

		final JButton btnPasteSnapshot = new JButton();
		btnPasteSnapshot.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/paste.png"));
		btnPasteSnapshot.setBorder(BorderFactory.createEmptyBorder());
		btnPasteSnapshot.setBackground(new Color(0, 0, 0, 1));
		btnPasteSnapshot.setToolTipText("Paste from clipboard");
		Action pasteAction = new PasteSnapAction();
		btnPasteSnapshot.addActionListener(pasteAction);

		botMgrPanel = new SimulationBotMgrPanel();

		add(btnToggleSim);
		add(btnStepBwd);
		add(btnStep);
		add(btnReset);
		add(btnLoadSnapshot);
		add(btnSaveSnapshot);
		add(btnCopySnapshot);
		add(btnPasteSnapshot);
		add(sliderSpeed);
		add(btnSlowmotion);
		add(lblSpeed);

		add(lblRelativeTime);
		add(lblTime);
		add(botMgrPanel);
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
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String txt = sdf.format(date);
		EventQueue.invokeLater(() -> lblTime.setText(txt));
	}


	/**
	 * Update the relative time label
	 *
	 * @param relTime
	 */
	public void updateRelativeTime(final double relTime)
	{
		EventQueue.invokeLater(() -> lblRelativeTime.setText(String.format("(x%4.2f)", relTime)));
	}


	/**
	 * Reset gui
	 */
	public void reset()
	{
		EventQueue.invokeLater(() -> {
			sliderSpeed.setValue(0);
			btnToggleSim.setSelected(false);
		});

		resetBotMgrPanel();
	}


	private void resetBotMgrPanel()
	{
		if (SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).isEmpty())
		{
			for (BotID botID : BotID.getAll())
			{
				botMgrPanel.setBotAvailable(botID, false);
			}
		} else
		{
			for (BotID botID : BotID.getAll())
			{
				botMgrPanel.setBotAvailable(botID,
						SumatraModel.getInstance().getModule(SumatraSimulator.class).isBotRegistered(botID));
			}
		}
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


	public SimulationBotMgrPanel getBotMgrPanel()
	{
		return botMgrPanel;
	}


	public interface ISimulationPanelObserver
	{
		/**
		 * Toggle resume/pause simulation
		 */
		void onToggleSimulation();

		/**
		 * Toggle slow motion
		 */
		void onToggleSlowMotion();

		/**
		 * @param speed
		 */
		void onChangeSpeed(double speed);


		/**
		 *
		 */
		void onStep();


		/**
		 *
		 */
		void onStepBwd();


		/**
		 * Reset the simulation
		 */
		void onReset();


		/**
		 *
		 */
		void onLoadSnapshot();


		/**
		 * Save current situation as snapshot
		 */
		void onSaveSnapshot();


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
			observers.forEach(ISimulationPanelObserver::onToggleSimulation);
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
				lblSpeed.setText(String.format("x1/%d", -(speed - 1)));
				simSpeed = 1.0 / -(speed - 1);
			} else if (speed == 0)
			{
				lblSpeed.setText("x1");
				simSpeed = 1;
			} else
			{
				lblSpeed.setText(String.format("x%d", speed + 1));
				simSpeed = speed + 1.0;
			}
			observers.forEach(o -> o.onChangeSpeed(simSpeed));
		}
	}

	private class StepAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onStep);
		}
	}

	private class StepBwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onStepBwd);
		}
	}

	private class ResetAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onReset);
		}
	}

	private class SaveSnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onSaveSnapshot);
		}
	}

	private class CopySnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onCopySnapshot);
		}
	}

	private class PasteSnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onPasteSnapshot);
			resetBotMgrPanel();
		}
	}


	private class OpenSnapAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onLoadSnapshot);
			resetBotMgrPanel();
		}
	}
}
