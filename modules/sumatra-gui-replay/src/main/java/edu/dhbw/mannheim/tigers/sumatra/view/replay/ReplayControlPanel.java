/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.replay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.tigers.sumatra.util.ImageScaler;


/**
 * This panel holds the control elements for the replay window
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlPanel extends JPanel implements IReplayPositionObserver
{
	private static final long								serialVersionUID		= 1L;
	
	private static final long								SKIP_TIME				= 500;
	private static final long								FAST_TIME				= 5_000L;
	private static final int								NUM_SPEED_FACTORS		= 4;
	private static final long								SLIDER_SCALE			= 100_000_000;
	
	private final List<IReplayControlPanelObserver>	observers				= new CopyOnWriteArrayList<IReplayControlPanelObserver>();
	
	private final JSlider									slider;
	
	
	private final JLabel										timeStepLabel			= new JLabel();
	private final JPanel										timeStepPanel			= createTimeStepPanel();
	private boolean											settingSliderByHand	= false;
	
	private final JButton									btnPlay					= new JButton();
	
	private double												speed						= 1;
	private boolean											playing					= true;
	
	
	/**
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
		
		btnPlay.addActionListener(new PlayButtonListener());
		btnPlay.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/pause.png"));
		btnPlay.setToolTipText("Pause [p]");
		btnPlay.setBorder(BorderFactory.createEmptyBorder());
		btnPlay.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnSkipFrameFwd = new JButton();
		btnSkipFrameFwd.addActionListener(new SkipFrameFwdListener());
		btnSkipFrameFwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameForward.png"));
		btnSkipFrameFwd.setToolTipText("Skip one frame forward [ctrl + right]");
		btnSkipFrameFwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipFrameFwd.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnSkipFrameBwd = new JButton();
		btnSkipFrameBwd.addActionListener(new SkipFrameBwdListener());
		btnSkipFrameBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipFrameBackward.png"));
		btnSkipFrameBwd.setToolTipText("Skip one frame backward [ctrl + left]");
		btnSkipFrameBwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipFrameBwd.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnSkipFwd = new JButton();
		btnSkipFwd.addActionListener(new SkipFwdListener());
		btnSkipFwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipForward.png"));
		btnSkipFwd.setToolTipText("Skip forward (" + SKIP_TIME + "ms) [right]");
		btnSkipFwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipFwd.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnSkipBwd = new JButton();
		btnSkipBwd.addActionListener(new SkipBwdListener());
		btnSkipBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/skipBackward.png"));
		btnSkipBwd.setToolTipText("Skip backward (" + SKIP_TIME + "ms) [left]");
		btnSkipBwd.setBorder(BorderFactory.createEmptyBorder());
		btnSkipBwd.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnFastFwd = new JButton();
		btnFastFwd.addActionListener(new FastFwdListener());
		btnFastFwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/fastForward.png"));
		btnFastFwd.setToolTipText("Fast forward (" + FAST_TIME + "ms) [shift + right]");
		btnFastFwd.setBorder(BorderFactory.createEmptyBorder());
		btnFastFwd.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnFastBwd = new JButton();
		btnFastBwd.addActionListener(new FastBwdListener());
		btnFastBwd.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/fastBackward.png"));
		btnFastBwd.setToolTipText("Fast backward (" + FAST_TIME + "ms) [shift + left]");
		btnFastBwd.setBorder(BorderFactory.createEmptyBorder());
		btnFastBwd.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnSnap = new JButton();
		btnSnap.addActionListener(new SnapshotListener());
		btnSnap.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/snap.png"));
		btnSnap.setToolTipText("Take and store snapshot");
		btnSnap.setBorder(BorderFactory.createEmptyBorder());
		btnSnap.setBackground(new Color(0, 0, 0, 1));
		
		JButton btnKickoff = new JButton("Kickoff");
		btnKickoff.setToolTipText("Search for next kickoff");
		btnKickoff.addActionListener(new KickoffListener());
		
		JCheckBox chkSkipStop = new JCheckBox("Skip");
		chkSkipStop.setToolTipText("Skip HALT and STOP gamestate");
		chkSkipStop.addActionListener(new SkipStopListener());
		
		JCheckBox chkRunCurrentAi = new JCheckBox("AI");
		chkRunCurrentAi.setToolTipText("Run AI over current Worldframe");
		chkRunCurrentAi.addActionListener(new RunCurrentAIListener());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(slider);
		topPanel.add(timeStepPanel);
		add(topPanel, BorderLayout.NORTH);
		
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
		bottomPanel.add(speedSlider);
		
		bottomPanel.add(chkRunCurrentAi);
		bottomPanel.add(chkSkipStop);
		bottomPanel.add(btnKickoff);
		add(bottomPanel, BorderLayout.CENTER);
		
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("typed p"), "playpause");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl RIGHT"), "skipFrameFwd");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl LEFT"), "skipFrameBwd");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "skipFwd");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "skipBwd");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("shift RIGHT"), "fastFwd");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("shift LEFT"), "fastBwd");
		getActionMap().put("playpause", new PlayButtonListener());
		getActionMap().put("skipFrameFwd", new SkipFrameFwdListener());
		getActionMap().put("skipFrameBwd", new SkipFrameBwdListener());
		getActionMap().put("skipFwd", new SkipFwdListener());
		getActionMap().put("skipBwd", new SkipBwdListener());
		getActionMap().put("fastFwd", new FastFwdListener());
		getActionMap().put("fastBwd", new FastBwdListener());
	}
	
	
	/**
	 * @return
	 */
	private JPanel createTimeStepPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("TimeStep"));
		panel.add(timeStepLabel);
		
		return panel;
	}
	
	
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
	public void addObserver(final IReplayControlPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IReplayControlPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class PlayButtonListener implements Action
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
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	
	private class SpeedSliderListener implements ChangeListener
	{
		
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			JSlider slider = (JSlider) e.getSource();
			int value = slider.getValue();
			double newSpeed = 1;
			for (int i = 0; i < value; i++)
			{
				newSpeed *= 2;
			}
			for (int i = 0; i < -value; i++)
			{
				newSpeed /= 2;
			}
			speed = newSpeed;
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSetSpeed(speed);
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
		}
		
		
		@Override
		public void mouseExited(final MouseEvent e)
		{
		}
	}
	
	
	private class SkipStopListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBox chk = (JCheckBox) e.getSource();
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSetSkipStop(chk.isSelected());
			}
		}
	}
	
	private class RunCurrentAIListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBox chk = (JCheckBox) e.getSource();
			for (IReplayControlPanelObserver o : observers)
			{
				o.onRunCurrentAi(chk.isSelected());
			}
		}
	}
	
	private class SkipFrameFwdListener implements Action
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onNextFrame();
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	private class SkipFrameBwdListener implements Action
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onPreviousFrame();
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	
	private class SkipFwdListener implements Action
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(SKIP_TIME * 1_000_000);
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	private class SkipBwdListener implements Action
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(-SKIP_TIME * 1_000_000);
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	private class FastFwdListener implements Action
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(FAST_TIME * 1_000_000);
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	private class FastBwdListener implements Action
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelativeTime(-FAST_TIME * 1_000_000);
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	private class SnapshotListener implements Action
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSnapshot();
			}
		}
		
		
		@Override
		public Object getValue(final String key)
		{
			return null;
		}
		
		
		@Override
		public void putValue(final String key, final Object value)
		{
		}
		
		
		@Override
		public void setEnabled(final boolean b)
		{
		}
		
		
		@Override
		public boolean isEnabled()
		{
			return true;
		}
		
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
		}
		
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener)
		{
		}
	}
	
	private class KickoffListener implements ActionListener
	{
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
