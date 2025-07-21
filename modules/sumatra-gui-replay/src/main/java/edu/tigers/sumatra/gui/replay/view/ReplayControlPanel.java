/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.replay.view;

import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.ImageScaler;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * This panel holds the control elements for the replay window
 */
public class ReplayControlPanel extends JPanel implements IReplayPositionObserver
{
	@Serial
	private static final long serialVersionUID = 1L;

	private static final long SKIP_TIME = 500;
	private static final long FAST_TIME = 5_000L;
	private static final int NUM_SPEED_FACTORS = 4;
	private static final long SLIDER_SCALE = 100_000_000;


	private final List<IReplayControlPanelObserver> observers = new CopyOnWriteArrayList<>();
	private final JSlider slider;


	private final JLabel timeStepLabel = new JLabel();
	private boolean settingSliderByHand = false;

	private final JButton btnPlay;

	private boolean playing = true;

	private final JMenu replayMenu = new JMenu("Replay");


	/**
	 * Default
	 */
	public ReplayControlPanel()
	{
		setLayout(new BorderLayout());

		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 1, 0);
		SliderListener sliderListener = new SliderListener();
		slider.addChangeListener(sliderListener);
		slider.addMouseListener(sliderListener);
		slider.setPreferredSize(new Dimension(2000, slider.getPreferredSize().height));

		JSlider speedSlider = createSpeedSlider();

		btnPlay = createActionButton(
				"Play / Pause",
				"/pause.png",
				this::togglePlayPause,
				KeyEvent.VK_P,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
		);

		JButton btnSkipFrameFwd = createActionButton(
				"Skip one frame forward",
				"/skipFrameForward.png",
				() -> observers.forEach(IReplayControlPanelObserver::onNextFrame),
				KeyEvent.VK_RIGHT,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
		);

		JButton btnSkipFrameBwd = createActionButton(
				"Skip one frame backward",
				"/skipFrameBackward.png",
				() -> observers.forEach(IReplayControlPanelObserver::onPreviousFrame),
				KeyEvent.VK_LEFT,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
		);

		JButton btnSkipFwd = createActionButton(
				"Skip forward (" + SKIP_TIME + "ms)",
				"/skipForward.png",
				() -> observers.forEach(o -> o.onChangeRelativeTime(SKIP_TIME * 1_000_000)),
				KeyEvent.VK_RIGHT,
				InputEvent.SHIFT_DOWN_MASK
		);

		JButton btnSkipBwd = createActionButton(
				"Skip backward (" + SKIP_TIME + "ms)",
				"/skipBackward.png",
				() -> observers.forEach(o -> o.onChangeRelativeTime(-SKIP_TIME * 1_000_000)),
				KeyEvent.VK_LEFT,
				InputEvent.SHIFT_DOWN_MASK
		);

		JButton btnFastFwd = createActionButton(
				"Fast forward (" + FAST_TIME + "ms)",
				"/fastForward.png",
				() -> observers.forEach(o -> o.onChangeRelativeTime(FAST_TIME * 1_000_000)),
				KeyEvent.VK_RIGHT,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
		);

		JButton btnFastBwd = createActionButton(
				"Fast backward (" + FAST_TIME + "ms)",
				"/fastBackward.png",
				() -> observers.forEach(o -> o.onChangeRelativeTime(-FAST_TIME * 1_000_000)),
				KeyEvent.VK_LEFT,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
		);

		JButton btnSnap = createActionButton(
				"Take and store snapshot",
				"/save.png",
				() -> observers.forEach(IReplayControlPanelObserver::onSnapshot),
				KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
		);

		JButton btnSnapCopy = createActionButton(
				"Copy snapshot to clipboard",
				"/copy.png",
				() -> observers.forEach(IReplayControlPanelObserver::onCopySnapshot),
				KeyEvent.VK_C,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
		);

		JButton btnCutReplay = createActionButton(
				"Create replay cut",
				"/icons8-scissors-60.png",
				() -> observers.forEach(IReplayControlPanelObserver::cutReplay),
				KeyEvent.VK_X,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
		);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(replayMenu);
		add(menuBar, BorderLayout.PAGE_START);

		JMenu jumpCommandMenu = new JMenu("Jump to Command");
		menuBar.add(jumpCommandMenu);
		Arrays.stream(SslGcRefereeMessage.Referee.Command.values())
				.map(JumpCommandAction::new)
				.forEach(jumpCommandMenu::add);
		JMenu jumpGameStateMenu = new JMenu("Jump to Game State");
		menuBar.add(jumpGameStateMenu);
		Arrays.stream(EGameState.values())
				.map(JumpGameStateAction::new)
				.forEach(jumpGameStateMenu::add);
		JMenu jumpGameEventMenu = new JMenu("Jump to Game Event");
		menuBar.add(jumpGameEventMenu);
		Arrays.stream(EGameEvent.values())
				.map(JumpGameEventAction::new)
				.forEach(jumpGameEventMenu::add);

		replayMenu.add(new SkipStopAction());
		replayMenu.add(new SkipBallPlacementAction());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(slider);
		final JPanel timeStepPanel = createTimeStepPanel();
		topPanel.add(timeStepPanel);
		add(topPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(btnFastBwd);
		bottomPanel.add(btnSkipBwd);
		bottomPanel.add(btnSkipFrameBwd);
		bottomPanel.add(btnPlay);
		bottomPanel.add(btnSkipFrameFwd);
		bottomPanel.add(btnSkipFwd);
		bottomPanel.add(btnFastFwd);
		bottomPanel.add(btnSnap);
		bottomPanel.add(btnSnapCopy);
		bottomPanel.add(btnCutReplay);
		bottomPanel.add(speedSlider);

		add(bottomPanel, BorderLayout.PAGE_END);
	}


	private JButton createActionButton(
			String description, String iconPath, Runnable action, int keyCode, int keyModifiers)
	{
		JButton button = new JButton();
		button.addActionListener(e -> action.run());
		button.setIcon(ImageScaler.scaleDefaultButtonImageIcon(iconPath));
		button.setToolTipText(description);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setBackground(new Color(0, 0, 0, 1));
		GlobalShortcuts.add(
				description,
				this,
				action,
				KeyStroke.getKeyStroke(keyCode, keyModifiers)
		);
		return button;
	}


	/**
	 * @param action
	 */
	public void addMenuCheckbox(Action action)
	{
		replayMenu.add(new JCheckBoxMenuItem(action));
	}


	/**
	 * @return
	 */
	private JPanel createTimeStepPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(timeStepLabel);
		return panel;
	}


	@SuppressWarnings("squid:S1149") // Hashtable -> dictated by swing
	private JSlider createSpeedSlider()
	{
		JSlider speedSlider = new JSlider(SwingConstants.HORIZONTAL, -NUM_SPEED_FACTORS, NUM_SPEED_FACTORS, 0);
		speedSlider.addChangeListener(new SpeedSliderListener());
		speedSlider.setMajorTickSpacing(1);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		speedSlider.setFont(new Font("", Font.PLAIN, 8));
		Dictionary<Integer, JLabel> dict = new Hashtable<>();
		DecimalFormat format = new DecimalFormat("0.###");
		for (int i = NUM_SPEED_FACTORS; i > 0; i--)
		{
			dict.put(-i, new JLabel(format.format(1f / Math.pow(2, i)) + "x"));
		}
		dict.put(0, new JLabel("1x"));
		for (int i = 1; i <= NUM_SPEED_FACTORS; i++)
		{
			dict.put(i, new JLabel(String.format("%dx", (int) Math.pow(2, i))));
		}
		Enumeration<JLabel> labels = dict.elements();
		while (labels.hasMoreElements())
		{
			labels.nextElement().setFont(speedSlider.getFont());
		}
		speedSlider.setLabelTable(dict);
		return speedSlider;
	}


	/**
	 * @param timeMax
	 */
	public void setTimeMax(final long timeMax)
	{
		slider.setMaximum((int) (timeMax / SLIDER_SCALE));
	}


	/**
	 * @param observer
	 */
	@SuppressWarnings("squid:S2250")
	public void addObserver(final IReplayControlPanelObserver observer)
	{
		observers.add(observer);
	}


	private void togglePlayPause()
	{
		playing = !playing;
		for (IReplayControlPanelObserver o : observers)
		{
			o.onPlayPause(playing);
		}
		if (playing)
		{
			btnPlay.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/pause.png"));
		} else
		{
			btnPlay.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/play.png"));
		}
		repaint();
	}


	private class SpeedSliderListener implements ChangeListener
	{

		@Override
		public void stateChanged(final ChangeEvent e)
		{
			JSlider jSlider = (JSlider) e.getSource();
			int value = jSlider.getValue();
			double newSpeed = 1;
			for (int i = 0; i < value; i++)
			{
				newSpeed *= 2;
			}
			for (int i = 0; i < -value; i++)
			{
				newSpeed /= 2;
			}
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSetSpeed(newSpeed);
			}
		}

	}

	private class SliderListener implements ChangeListener, MouseListener
	{
		private int value = 0;


		@Override
		public void stateChanged(final ChangeEvent e)
		{
			if (settingSliderByHand)
			{
				value = slider.getValue();
				for (IReplayControlPanelObserver o : observers)
				{
					o.onChangeAbsoluteTime(value * SLIDER_SCALE);
				}
			}
		}


		@Override
		public void mouseClicked(final MouseEvent e)
		{
			// ignored
		}


		@Override
		public void mousePressed(final MouseEvent e)
		{
			settingSliderByHand = true;
			value = slider.getValue();
		}


		@Override
		public void mouseReleased(final MouseEvent e)
		{
			settingSliderByHand = false;
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeAbsoluteTime(value * SLIDER_SCALE);
			}
		}


		@Override
		public void mouseEntered(final MouseEvent e)
		{
			// ignored
		}


		@Override
		public void mouseExited(final MouseEvent e)
		{
			// ignored
		}
	}


	private class SkipStopAction extends AbstractAction
	{
		public SkipStopAction()
		{
			super("Skip STOP state");
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSetSkipStop(chk.isSelected());
			}
		}
	}

	private class SkipBallPlacementAction extends AbstractAction
	{
		public SkipBallPlacementAction()
		{
			super("Skip Ball Placement");
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSetSkipBallPlacement(chk.isSelected());
			}
		}
	}

	private class JumpCommandAction extends AbstractAction
	{
		private final SslGcRefereeMessage.Referee.Command command;


		public JumpCommandAction(SslGcRefereeMessage.Referee.Command command)
		{
			super(command.name());
			this.command = command;
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSearchCommand(command);
			}
		}
	}

	private class JumpGameEventAction extends AbstractAction
	{
		private final EGameEvent gameEvent;


		public JumpGameEventAction(EGameEvent gameEvent)
		{
			super(gameEvent.name());
			this.gameEvent = gameEvent;
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSearchGameEvent(gameEvent);
			}
		}
	}

	private class JumpGameStateAction extends AbstractAction
	{
		private final EGameState gameState;


		public JumpGameStateAction(EGameState gameState)
		{
			super(gameState.name());
			this.gameState = gameState;
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSearchGameState(gameState);
			}
		}
	}


	@Override
	public void onPositionChanged(final long position)
	{
		if (!settingSliderByHand)
		{
			slider.setValue((int) (position / SLIDER_SCALE));
		}
	}


	/**
	 * @return the timeStepLabel
	 */
	public final JLabel getTimeStepLabel()
	{
		return timeStepLabel;
	}
}
