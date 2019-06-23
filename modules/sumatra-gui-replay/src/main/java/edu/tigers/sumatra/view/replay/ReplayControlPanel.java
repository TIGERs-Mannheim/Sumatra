/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.replay;

import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * This panel holds the control elements for the replay window
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlPanel extends JPanel implements IReplayPositionObserver, ISumatraView
{
	private static final long serialVersionUID = 1L;
	
	private static final long SKIP_TIME = 500;
	private static final long FAST_TIME = 5_000L;
	private static final int NUM_SPEED_FACTORS = 4;
	private static final long SLIDER_SCALE = 100_000_000;
	
	
	private final List<IReplayControlPanelObserver> observers = new CopyOnWriteArrayList<>();
	private final JSlider slider;
	
	
	private final JLabel timeStepLabel = new JLabel();
	private boolean settingSliderByHand = false;
	
	private final JButton btnPlay = new JButton();
	
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
		
		Action playAction = new PlayAction();
		btnPlay.addActionListener(playAction);
		btnPlay.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/pause.png"));
		btnPlay.setToolTipText("Play/Pause");
		btnPlay.setBorder(BorderFactory.createEmptyBorder());
		btnPlay.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnPlay, getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), playAction);
		
		JButton btnSkipFrameFwd = new JButton();
		Action skipOneFrameFwdAction = new SkipOneFrameFwdAction();
		btnSkipFrameFwd.addActionListener(skipOneFrameFwdAction);
		btnSkipFrameFwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameForward.png"));
		btnSkipFrameFwd.setToolTipText("Skip one frame forward");
		btnSkipFrameFwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipFrameFwd.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnSkipFrameFwd, getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
				skipOneFrameFwdAction);
		
		JButton btnSkipFrameBwd = new JButton();
		final Action skipOneFrameBwdAction = new SkipOneFrameBwdAction();
		btnSkipFrameBwd.addActionListener(skipOneFrameBwdAction);
		btnSkipFrameBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameBackward.png"));
		btnSkipFrameBwd.setToolTipText("Skip one frame backward");
		btnSkipFrameBwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipFrameBwd.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnSkipFrameBwd, getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), skipOneFrameBwdAction);
		
		JButton btnSkipFwd = new JButton();
		final Action skipFwdAction = new SkipFwdAction();
		btnSkipFwd.addActionListener(skipFwdAction);
		btnSkipFwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipForward.png"));
		btnSkipFwd.setToolTipText("Skip forward (" + SKIP_TIME + "ms)");
		btnSkipFwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipFwd.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnSkipFwd, getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), skipFwdAction);
		
		JButton btnSkipBwd = new JButton();
		final SkipBwdAction skipBwdAction = new SkipBwdAction();
		btnSkipBwd.addActionListener(skipBwdAction);
		btnSkipBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipBackward.png"));
		btnSkipBwd.setToolTipText("Skip backward (" + SKIP_TIME + "ms)");
		btnSkipBwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipBwd.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnSkipBwd, getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), skipBwdAction);
		
		JButton btnFastFwd = new JButton();
		final FastFwdAction fastFwdAction = new FastFwdAction();
		btnFastFwd.addActionListener(fastFwdAction);
		btnFastFwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/fastForward.png"));
		btnFastFwd.setToolTipText("Fast forward (" + FAST_TIME + "ms)");
		btnFastFwd.setBorder(BorderFactory.createEmptyBorder());
		btnFastFwd.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnFastFwd, getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				fastFwdAction);
		
		JButton btnFastBwd = new JButton();
		final Action fastBwdAction = new FastBwdAction();
		btnFastBwd.addActionListener(fastBwdAction);
		btnFastBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/fastBackward.png"));
		btnFastBwd.setToolTipText("Fast backward (" + FAST_TIME + "ms)");
		btnFastBwd.setBorder(BorderFactory.createEmptyBorder());
		btnFastBwd.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnFastBwd, getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				fastBwdAction);
		
		JButton btnSnap = new JButton();
		final SnapshotAction snapshotAction = new SnapshotAction();
		btnSnap.addActionListener(snapshotAction);
		btnSnap.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/save.png"));
		btnSnap.setToolTipText("Take and store snapshot");
		btnSnap.setBorder(BorderFactory.createEmptyBorder());
		btnSnap.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnSnap, getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), snapshotAction);
		
		JButton btnSnapCopy = new JButton();
		final Action snapshotCopyAction = new SnapshotCopyAction();
		btnSnapCopy.addActionListener(snapshotCopyAction);
		btnSnapCopy.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/copy.png"));
		btnSnapCopy.setToolTipText("Copy snapshot to clipboard");
		btnSnapCopy.setBorder(BorderFactory.createEmptyBorder());
		btnSnapCopy.setBackground(new Color(0, 0, 0, 1));
		registerShortcut(btnSnapCopy, getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), snapshotCopyAction);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(replayMenu);
		add(menuBar, BorderLayout.PAGE_START);
		
		addMenuAction(new KickoffAction());
		addMenuCheckbox(new SkipStopAction());
		addMenuCheckbox(new SkipBallPlacementAction());
		
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
		bottomPanel.add(speedSlider);
		
		add(bottomPanel, BorderLayout.PAGE_END);
	}
	
	
	/**
	 * @param action an action
	 */
	private void addMenuAction(Action action)
	{
		replayMenu.add(action);
	}
	
	
	/**
	 * @param action
	 */
	public void addMenuCheckbox(Action action)
	{
		replayMenu.add(new JCheckBoxMenuItem(action));
	}
	
	
	private void registerShortcut(JComponent component, KeyStroke keyStroke, Action action)
	{
		String actionCommand = action.getClass().getCanonicalName();
		component.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionCommand);
		component.getActionMap().put(actionCommand, action);
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
	
	
	private class PlayAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
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
	
	
	private class SkipOneFrameFwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onNextFrame();
			}
		}
	}
	
	private class SkipOneFrameBwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onPreviousFrame();
			}
		}
	}
	
	
	private class SkipFwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(SKIP_TIME * 1_000_000);
			}
		}
	}
	
	private class SkipBwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(-SKIP_TIME * 1_000_000);
			}
		}
	}
	
	private class FastFwdAction extends AbstractAction
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(FAST_TIME * 1_000_000);
			}
		}
	}
	
	private class FastBwdAction extends AbstractAction
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(-FAST_TIME * 1_000_000);
			}
		}
	}
	
	private class SnapshotAction extends AbstractAction
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSnapshot();
			}
		}
	}
	
	private class SnapshotCopyAction extends AbstractAction
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onCopySnapshot();
			}
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
	
	private class KickoffAction extends AbstractAction
	{
		public KickoffAction()
		{
			super("Next kickoff");
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSearchKickoff(true);
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
