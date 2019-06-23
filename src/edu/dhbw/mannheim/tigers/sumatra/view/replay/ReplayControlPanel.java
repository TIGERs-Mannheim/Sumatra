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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * This panel holds the control elements for the replay window
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlPanel extends JPanel implements IReplayPositionObserver
{
	private static final long								serialVersionUID		= 1L;
	private static final int								MAX_SCROLL_SPEED		= 20;
	private final List<IReplayControlPanelObserver>	observers				= new CopyOnWriteArrayList<IReplayControlPanelObserver>();
	private final JSlider									slider;
	private final JSlider									speedCtrlSlider;
	private final JLabel										timeStepLabel			= new JLabel();
	private final JPanel										timeStepPanel			= createTimeStepPanel();
	private boolean											settingSliderByHand	= false;
	
	
	/**
	 */
	public ReplayControlPanel()
	{
		setLayout(new BorderLayout());
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 1, 0);
		SliderListener sliderListener = new SliderListener();
		slider.addChangeListener(sliderListener);
		slider.addMouseListener(sliderListener);
		
		speedCtrlSlider = new JSlider(SwingConstants.HORIZONTAL, -MAX_SCROLL_SPEED, MAX_SCROLL_SPEED, 0);
		speedCtrlSlider.setPaintTrack(false);
		speedCtrlSlider.setPaintTicks(true);
		speedCtrlSlider.setMajorTickSpacing(MAX_SCROLL_SPEED);
		SpeedCtrlSliderListener speedCtrlListener = new SpeedCtrlSliderListener();
		speedCtrlSlider.addChangeListener(speedCtrlListener);
		speedCtrlSlider.addMouseListener(speedCtrlListener);
		speedCtrlSlider.setMaximumSize(new Dimension(4000, speedCtrlSlider.getMaximumSize().height));
		speedCtrlSlider.setPreferredSize(new Dimension(4000, speedCtrlSlider.getMaximumSize().height));
		
		JToggleButton btnPlay = new JToggleButton("Pause");
		btnPlay.setSelected(true);
		btnPlay.addActionListener(new PlayButtonListener());
		JButton btnFaster = new JButton("Faster");
		btnFaster.addActionListener(new FasterButtonListener());
		JButton btnSlower = new JButton("Slower");
		btnSlower.addActionListener(new SlowerButtonListener());
		JButton btnOneFrame = new JButton("Next Frame");
		btnOneFrame.addActionListener(new NextFrameListener());
		JButton btnKickoff = new JButton("Find kickoff");
		btnKickoff.addActionListener(new KickoffListener());
		JCheckBox chkSkipStop = new JCheckBox("skip stop");
		chkSkipStop.addActionListener(new SkipStopListener());
		JCheckBox chkRunCurrentAi = new JCheckBox("run current AI");
		chkRunCurrentAi.addActionListener(new RunCurrentAIListener());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(slider);
		topPanel.add(timeStepPanel);
		add(topPanel, BorderLayout.NORTH);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(speedCtrlSlider);
		bottomPanel.add(chkRunCurrentAi);
		bottomPanel.add(chkSkipStop);
		bottomPanel.add(btnKickoff);
		bottomPanel.add(btnOneFrame);
		bottomPanel.add(btnPlay);
		bottomPanel.add(btnFaster);
		bottomPanel.add(btnSlower);
		add(bottomPanel, BorderLayout.CENTER);
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
	
	
	/**
	 * @param numFrames
	 */
	public void setNumFrames(final int numFrames)
	{
		slider.setMaximum(numFrames);
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
	
	private class PlayButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (e.getSource() instanceof JToggleButton)
			{
				JToggleButton src = (JToggleButton) e.getSource();
				for (IReplayControlPanelObserver o : observers)
				{
					o.onPlayStateChanged(src.isSelected());
					if (src.isSelected())
					{
						src.setText("Pause");
					} else
					{
						src.setText("Play ");
					}
				}
			}
		}
		
	}
	
	private class FasterButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onFaster();
			}
		}
		
	}
	
	private class SlowerButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSlower();
			}
		}
		
	}
	
	private class SliderListener implements ChangeListener, MouseListener
	{
		private int	value	= 0;
		
		
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			if (settingSliderByHand)
			{
				value = slider.getValue();
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
				o.onPositionChanged(value);
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
	
	private class SpeedCtrlSliderListener implements ChangeListener, MouseListener
	{
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onChangeRelPos(speedCtrlSlider.getValue());
			}
		}
		
		
		@Override
		public void mouseClicked(final MouseEvent e)
		{
		}
		
		
		@Override
		public void mousePressed(final MouseEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.setFrameByFrame(true);
			}
		}
		
		
		@Override
		public void mouseReleased(final MouseEvent e)
		{
			speedCtrlSlider.setValue(0);
			for (IReplayControlPanelObserver o : observers)
			{
				o.setFrameByFrame(false);
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
	
	private class NextFrameListener implements ActionListener
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
	
	private class KickoffListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSearchKickoff();
			}
		}
	}
	
	
	@Override
	public void onPositionChanged(final int position)
	{
		if (!settingSliderByHand)
		{
			slider.setValue(position);
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
