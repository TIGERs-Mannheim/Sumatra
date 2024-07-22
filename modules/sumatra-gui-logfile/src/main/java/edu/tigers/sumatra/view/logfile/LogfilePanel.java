/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.logfile;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Panel for log file viewing
 */
@Log4j2
public class LogfilePanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 7096879604458197996L;
	private static final String SSL_LOG_FOLDER = ".sslLogFolder";

	private final JToggleButton btnPause;
	private final JSlider sliderSpeed;
	private final JProgressBar position;
	private final JLabel labelSpeed;
	private final JButton btnLoadLogfile;
	private final JFileChooser fcOpenLogfile;
	private final JFileChooser fcSelectMergeFiles;
	private final MergeToolAuxPanel mergeToolAux;
	private final JLabel lblTime;

	private final transient List<ILogfilePanelObserver> observers = new CopyOnWriteArrayList<>();


	public LogfilePanel()
	{
		setLayout(new MigLayout("fill, wrap 2", "[]10[grow, align center]"));

		btnPause = new JToggleButton("Pause");
		btnPause.addActionListener(new PauseAction());

		final JButton btnNext = new JButton(">");
		btnNext.addActionListener(new StepAction(1));

		final JButton btnPrev = new JButton("<");
		btnPrev.addActionListener(new StepAction(-1));

		final JButton btnNextFew = new JButton(">>");
		btnNextFew.addActionListener(new StepAction(10));

		final JButton btnPrevFew = new JButton("<<");
		btnPrevFew.addActionListener(new StepAction(-10));

		final JButton btnPrevMuch = new JButton("<<<");
		btnPrevMuch.addActionListener(new StepAction(-100));

		final JButton btnNextMuch = new JButton(">>>");
		btnNextMuch.addActionListener(new StepAction(100));

		final JButton btnSkipToDirectIndirect = new JButton("Seek to Freekick");
		btnSkipToDirectIndirect.addActionListener(
				new SeekAction(new Command[] { Command.DIRECT_FREE_YELLOW, Command.DIRECT_FREE_BLUE }));

		JComboBox<SslGcGameEvent.GameEvent.Type> cboGameEvent = new JComboBox<>(SslGcGameEvent.GameEvent.Type.values());
		JButton btnSeekGameEvent = new JButton();
		btnSeekGameEvent.setAction(new SeekGameEventAction(cboGameEvent));

		sliderSpeed = new JSlider(-9, 9, 0);
		sliderSpeed.setMajorTickSpacing(5);
		sliderSpeed.setMinorTickSpacing(1);
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setPaintTicks(true);
		sliderSpeed.addChangeListener(new SpeedListener());

		labelSpeed = new JLabel("x1");
		labelSpeed.setPreferredSize(new Dimension(40, labelSpeed.getMaximumSize().height));

		lblTime = new JLabel("0");
		lblTime.setPreferredSize(new Dimension(100, lblTime.getMaximumSize().height));

		btnLoadLogfile = new JButton("Load");
		btnLoadLogfile.addActionListener(new OpenFileAction());

		final JButton btnMergeTool = new JButton("Merge Tool");
		btnMergeTool.addActionListener(new MergeAction());

		position = new JProgressBar(0, 1000);
		position.setStringPainted(true);
		position.addMouseListener(new PosListener());

		String mainCfgPath = SumatraModel.getInstance().getUserProperty(
				LogfilePanel.class.getCanonicalName() + SSL_LOG_FOLDER);
		if (mainCfgPath != null)
		{
			fcOpenLogfile = new JFileChooser(mainCfgPath);
			fcSelectMergeFiles = new JFileChooser(mainCfgPath);
		} else
		{
			fcOpenLogfile = new JFileChooser();
			fcSelectMergeFiles = new JFileChooser();
		}

		fcOpenLogfile.setFileFilter(new FileNameExtensionFilter("Logfiles", "log", "gz"));

		mergeToolAux = new MergeToolAuxPanel();
		fcSelectMergeFiles.setFileFilter(new FileNameExtensionFilter("Logfiles", "log", "gz"));
		fcSelectMergeFiles.setAccessory(mergeToolAux);
		fcSelectMergeFiles.setMultiSelectionEnabled(true);
		fcSelectMergeFiles.setDialogTitle("Merge Tool");
		fcSelectMergeFiles.setApproveButtonText("Merge");


		JPanel ctrl = new JPanel(new MigLayout());
		ctrl.add(btnPrevMuch);
		ctrl.add(btnPrevFew);
		ctrl.add(btnPrev);
		ctrl.add(btnPause, "gap 30 30");
		ctrl.add(btnNext);
		ctrl.add(btnNextFew);
		ctrl.add(btnNextMuch);
		ctrl.add(sliderSpeed, "aligny top");
		ctrl.add(labelSpeed);

		JPanel ctrlEvents = new JPanel(new MigLayout());
		ctrlEvents.add(btnSkipToDirectIndirect, "gapright 50");
		ctrlEvents.add(cboGameEvent);
		ctrlEvents.add(btnSeekGameEvent);

		add(lblTime);
		add(position, "pushx, grow");

		add(btnLoadLogfile);
		add(ctrl);

		add(btnMergeTool);
		add(ctrlEvents);

		add(Box.createGlue(), "push");
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

		lblTime.setText(txt);
	}


	public void setNumPackets(final int num)
	{
		position.setMaximum(num);
		position.setString("0 / " + num);
	}


	public void setPosition(final int pos)
	{
		position.setValue(pos);
		position.setString(pos + " / " + position.getMaximum());
	}


	/**
	 * @param observer
	 */
	public void addObserver(final ILogfilePanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final ILogfilePanelObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * @author AndreR
	 */
	public interface ILogfilePanelObserver
	{
		/**
		 * Pause playback.
		 */
		void onPause();


		/**
		 * Resume playback.
		 */
		void onResume();


		/**
		 * @param speed
		 */
		void onChangeSpeed(double speed);


		/**
		 * @param numSteps
		 */
		void onStep(int numSteps);


		/**
		 * @param path
		 */
		void onLoadLogfile(String path);


		/**
		 * @param pos
		 */
		void onChangePosition(int pos);


		/**
		 * Seek to a specific referee message(s).
		 *
		 * @param commands
		 */
		void onSeekToRefCmd(List<Referee.Command> commands);


		/**
		 * Seek to a specific game event(s).
		 *
		 * @param gameEventTypes
		 */
		void onSeekToGameEvent(final List<SslGcGameEvent.GameEvent.Type> gameEventTypes);


		/**
		 * Merge multiple log files.
		 *
		 * @param inputs
		 * @param output
		 * @param removeIdle
		 */
		void onMergeFiles(List<String> inputs, String output, boolean removeIdle);
	}

	private class PauseAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (btnPause.isSelected())
			{
				for (ILogfilePanelObserver o : observers)
				{
					o.onPause();
				}
			} else
			{
				for (ILogfilePanelObserver o : observers)
				{
					o.onResume();
				}
			}
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
				speed -= 1;
				labelSpeed.setText(String.format("x1/%d", -speed));
				simSpeed = 1.0 / -speed;
			} else if (speed == 0)
			{
				speed = 1;
				labelSpeed.setText(String.format("x%d", speed));
				simSpeed = 1;
			} else
			{
				speed += 1;
				labelSpeed.setText(String.format("x%d", speed));
				simSpeed = speed;
			}
			for (ILogfilePanelObserver o : observers)
			{
				o.onChangeSpeed(simSpeed);
			}
		}
	}

	private class PosListener extends MouseAdapter
	{
		@Override
		public void mouseReleased(final MouseEvent e)
		{
			btnPause.setSelected(false);

			// Retrieves the mouse position relative to the component origin.
			int mouseX = e.getX();

			// Computes how far along the mouse is relative to the component width then multiply it by the progress bar's
			// maximum value.
			int newPosition = (int) Math.round(((double) mouseX / (double) position.getWidth()) * position.getMaximum());

			for (ILogfilePanelObserver o : observers)
			{
				o.onChangePosition(newPosition);
				o.onResume();
			}
		}
	}


	private class StepAction implements ActionListener
	{
		private final int numSteps;


		public StepAction(final int numSteps)
		{
			this.numSteps = numSteps;
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			btnPause.setSelected(true);

			for (ILogfilePanelObserver o : observers)
			{
				o.onStep(numSteps);
				o.onPause();
			}
		}
	}

	private class SeekAction implements ActionListener
	{
		private final List<Referee.Command> commands;


		public SeekAction(final Referee.Command[] commands)
		{
			this.commands = Arrays.asList(commands);
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			btnPause.setSelected(true);

			for (ILogfilePanelObserver o : observers)
			{
				o.onSeekToRefCmd(commands);
			}
		}
	}

	private class OpenFileAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int returnVal = fcOpenLogfile.showOpenDialog(btnLoadLogfile);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fcOpenLogfile.getSelectedFile();

				SumatraModel.getInstance().setUserProperty(
						LogfilePanel.class.getCanonicalName() + SSL_LOG_FOLDER, file.getAbsolutePath());

				for (ILogfilePanelObserver o : observers)
				{
					try
					{
						o.onLoadLogfile(file.getCanonicalPath());
					} catch (IOException e1)
					{
						log.error("", e1);
					}
				}
			}
		}
	}

	private class MergeAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int returnVal = fcSelectMergeFiles.showOpenDialog(btnLoadLogfile);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File[] files = fcSelectMergeFiles.getSelectedFiles();

				SumatraModel.getInstance().setUserProperty(
						LogfilePanel.class.getCanonicalName() + SSL_LOG_FOLDER, files[0].getAbsolutePath());

				if (mergeToolAux.outputFilename.getText().isEmpty())
				{
					return;
				}

				String output = files[0].getParentFile().getAbsolutePath()
						+ File.separator + mergeToolAux.outputFilename.getText() + ".log";

				List<String> inputs = new ArrayList<>();

				for (File f : files)
				{
					inputs.add(f.getAbsolutePath());
				}

				for (ILogfilePanelObserver o : observers)
				{
					o.onMergeFiles(inputs, output, mergeToolAux.removeIdle.isSelected());
				}
			}
		}
	}

	private class SeekGameEventAction extends AbstractAction
	{
		@Serial
		private static final long serialVersionUID = -961064147040314494L;
		private final JComboBox<SslGcGameEvent.GameEvent.Type> gameEventCombo;


		private SeekGameEventAction(final JComboBox<SslGcGameEvent.GameEvent.Type> gameEventCombo)
		{
			super("Next Game Event");
			this.gameEventCombo = gameEventCombo;
		}


		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			for (ILogfilePanelObserver o : observers)
			{
				o.onSeekToGameEvent(
						Collections.singletonList((SslGcGameEvent.GameEvent.Type) gameEventCombo.getSelectedItem()));
			}
		}
	}

	private static class MergeToolAuxPanel extends JPanel
	{
		@Serial
		private static final long serialVersionUID = -4340436333055730063L;

		private final JCheckBox removeIdle;
		private final JTextField outputFilename;


		private MergeToolAuxPanel()
		{
			setLayout(new MigLayout("wrap 1"));

			removeIdle = new JCheckBox("Remove Idle Stages", true);
			outputFilename = new JTextField();

			add(new JLabel("Output Filename:"));
			add(outputFilename, "w 200");
			add(removeIdle);
		}
	}
}
