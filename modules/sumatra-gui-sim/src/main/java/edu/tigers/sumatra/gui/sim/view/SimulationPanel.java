package edu.tigers.sumatra.gui.sim.view;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SumatraSimulator;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private static final long serialVersionUID = 4936408022928626573L;

	@Getter
	private final JToggleButton btnToggleSim;
	@Getter
	private final JSlider sliderSpeed;
	private final JLabel lblSpeed;
	private final JLabel lblTime;
	private final JLabel lblRelativeTime;
	@Getter
	private final JToggleButton btnSlowmotion;
	@Getter
	private final JCheckBox restoreLastSnapshot = new JCheckBox("Restore on start", true);

	private final JButton btnStepBwd = new JButton();
	private final JButton btnStep = new JButton();
	private final JButton btnRepublish = new JButton();
	private final JButton btnReset = new JButton();
	private final JButton btnLoadSnapshot = new JButton();
	private final JButton btnSaveSnapshot = new JButton();
	private final JButton btnCopySnapshot = new JButton();
	private final JButton btnPasteSnapshot = new JButton();

	private final SimulationBotMgrPanel botMgrPanel;

	private final transient List<ISimulationPanelObserver> observers = new CopyOnWriteArrayList<>();

	private boolean initialized = false;


	public SimulationPanel()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));

		btnToggleSim = new JToggleButton();
		btnSlowmotion = new JToggleButton();

		btnToggleSim.setToolTipText("Pause/Play");
		btnStepBwd.setToolTipText("Step backward");
		btnStep.setToolTipText("Step forward");
		btnRepublish.setToolTipText("Republish");
		btnReset.setToolTipText("Reset simulation");

		btnSlowmotion.setToolTipText("Toggle slow motion");

		btnLoadSnapshot.setToolTipText("Open from file");
		btnSaveSnapshot.setToolTipText("Save to file");
		btnCopySnapshot.setToolTipText("Copy to clipboard");
		btnPasteSnapshot.setToolTipText("Paste from clipboard");

		// actions
		btnToggleSim.addActionListener(new PauseSimulationAction());
		btnStepBwd.addActionListener(new StepBwdAction());
		btnStep.addActionListener(new StepAction());
		btnRepublish.addActionListener(new RepublishAction());
		btnReset.addActionListener(new ResetAction());
		btnLoadSnapshot.addActionListener(new OpenSnapAction());
		btnSaveSnapshot.addActionListener(new SaveSnapAction());
		btnCopySnapshot.addActionListener(new CopySnapAction());
		btnPasteSnapshot.addActionListener(new PasteSnapAction());

		btnSlowmotion.addActionListener(e -> observers.forEach(ISimulationPanelObserver::onToggleSlowMotion));

		restoreLastSnapshot.addActionListener(new RestoreLastSnapshotListener());

		// slider
		sliderSpeed = new JSlider(-9, 1, 0);
		sliderSpeed.setMajorTickSpacing(5);
		sliderSpeed.setMinorTickSpacing(1);
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setPaintTicks(true);
		sliderSpeed.addChangeListener(new SpeedListener());

		lblSpeed = new JLabel("x1");
		lblRelativeTime = new JLabel("(x1)");
		lblTime = new JLabel("-");

		botMgrPanel = new SimulationBotMgrPanel();

		// layout
		add(btnToggleSim);
		add(btnStepBwd);
		add(btnStep);
		add(btnRepublish);
		add(btnReset);
		add(btnLoadSnapshot);
		add(btnSaveSnapshot);
		add(btnCopySnapshot);
		add(btnPasteSnapshot);
		add(restoreLastSnapshot);
		add(sliderSpeed);
		add(btnSlowmotion);
		add(lblSpeed);
		add(lblRelativeTime);
		add(lblTime);
		add(botMgrPanel);

		initialized = true;
		setRunning(false);
		updateIcons();
	}


	@Override
	public void updateUI()
	{
		super.updateUI();
		if (initialized)
		{
			updateIcons();
		}
	}


	private void updateIcons()
	{
		Color c = UIManager.getColor("Label.foreground");

		updateToggleSimIcon(c);

		btnToggleSim.setIcon(IconFontSwing.buildIcon(FontAwesome.PLAY, 22, c));
		updateToggleSimIcon(c);

		btnStepBwd.setIcon(IconFontSwing.buildIcon(FontAwesome.STEP_BACKWARD, 22, c));
		btnStep.setIcon(IconFontSwing.buildIcon(FontAwesome.STEP_FORWARD, 22, c));

		btnRepublish.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 22, c));
		btnReset.setIcon(IconFontSwing.buildIcon(FontAwesome.UNDO, 22, c));

		btnLoadSnapshot.setIcon(IconFontSwing.buildIcon(FontAwesome.FOLDER_OPEN, 22, c));
		btnSaveSnapshot.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 22, c));
		btnCopySnapshot.setIcon(IconFontSwing.buildIcon(FontAwesome.CLIPBOARD, 22, c));
		btnPasteSnapshot.setIcon(IconFontSwing.buildIcon(FontAwesome.FILES_O, 22, c));

		btnSlowmotion.setIcon(IconFontSwing.buildIcon(FontAwesome.CLOCK_O, 22, c));
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
		lblTime.setText(txt);
	}


	/**
	 * Update the relative time label
	 *
	 * @param relTime
	 */
	public void updateRelativeTime(final double relTime)
	{
		lblRelativeTime.setText(String.format("(x%4.2f)", relTime));
	}


	/**
	 * Reset gui
	 */
	public void reset()
	{
		sliderSpeed.setValue(0);
		btnToggleSim.setSelected(false);
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
				botMgrPanel.setBotAvailable(
						botID,
						SumatraModel.getInstance().getModule(SumatraSimulator.class).isBotRegistered(botID)
				);
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


	private Icon iconPlay(Color c)
	{
		return IconFontSwing.buildIcon(FontAwesome.PLAY, 22, c);
	}


	private Icon iconPause(Color c)
	{
		return IconFontSwing.buildIcon(FontAwesome.PAUSE, 22, c);
	}


	private void updateToggleSimIcon(Color c)
	{
		btnToggleSim.setIcon(
				btnToggleSim.isSelected()
						? iconPause(c)
						: iconPlay(c)
		);
	}


	public void setRunning(boolean running)
	{
		btnToggleSim.setSelected(running);

		Color c = UIManager.getColor("Label.foreground");
		updateToggleSimIcon(c);
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
		 * Republish the current simulation state again
		 */
		void onRepublish();

		/**
		 *
		 */
		void onLoadSnapshot();

		/**
		 * Save current situation as snapshot
		 */
		void onSaveSnapshot();

		/**
		 * Save the current situation as the last snapshot that will be restored on next start
		 */
		void onSaveLastSnapshot();

		/**
		 * Copy snapshot to clipboard
		 */
		void onCopySnapshot();

		/**
		 * Paste snapshot from clipboard
		 */
		void onPasteSnapshot();

		/**
		 * Set if last snapshot should be restored on startup
		 *
		 * @param active
		 */
		void onSetRestoreLastSnapshot(boolean active);
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
				simSpeed = Double.POSITIVE_INFINITY;
				lblSpeed.setText("Max");
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

	private class RepublishAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(ISimulationPanelObserver::onRepublish);
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


	private class RestoreLastSnapshotListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBox checkBox = (JCheckBox) e.getSource();
			observers.forEach(o -> o.onSetRestoreLastSnapshot(checkBox.isSelected()));
		}
	}
}
