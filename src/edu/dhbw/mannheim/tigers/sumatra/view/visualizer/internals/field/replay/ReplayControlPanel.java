/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long								serialVersionUID	= 1L;
	private static final int								MAX_SCROLL_SPEED	= 20;
	private final List<IReplayControlPanelObserver>	observers			= new CopyOnWriteArrayList<IReplayControlPanelObserver>();
	private final JSlider									slider;
	private final JSlider									speedCtrlSlider;
	
	private boolean											sliderManuelSet	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param numFrames
	 */
	public ReplayControlPanel(final int numFrames)
	{
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, numFrames, 0);
		slider.addChangeListener(new SliderListener());
		
		speedCtrlSlider = new JSlider(SwingConstants.HORIZONTAL, -MAX_SCROLL_SPEED, MAX_SCROLL_SPEED, 0);
		speedCtrlSlider.setPaintTrack(false);
		speedCtrlSlider.setPaintTicks(true);
		speedCtrlSlider.setMajorTickSpacing(MAX_SCROLL_SPEED);
		SpeedCtrlSliderListener speedCtrlListener = new SpeedCtrlSliderListener();
		speedCtrlSlider.addChangeListener(speedCtrlListener);
		speedCtrlSlider.addMouseListener(speedCtrlListener);
		speedCtrlSlider.setMaximumSize(new Dimension(300, speedCtrlSlider.getMaximumSize().height));
		
		JToggleButton btnPlay = new JToggleButton("Pause");
		btnPlay.setSelected(true);
		btnPlay.addActionListener(new PlayButtonListener());
		JButton btnFaster = new JButton("Faster");
		btnFaster.addActionListener(new FasterButtonListener());
		JButton btnSlower = new JButton("Slower");
		btnSlower.addActionListener(new SlowerButtonListener());
		
		add(slider);
		add(speedCtrlSlider);
		add(btnPlay);
		add(btnFaster);
		add(btnSlower);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
	
	private class SliderListener implements ChangeListener
	{
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			if (!sliderManuelSet)
			{
				if (e.getSource() instanceof JSlider)
				{
					JSlider src = (JSlider) e.getSource();
					for (IReplayControlPanelObserver o : observers)
					{
						o.onPositionChanged(src.getValue());
					}
				}
			}
			sliderManuelSet = false;
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
		}
		
		
		@Override
		public void mouseReleased(final MouseEvent e)
		{
			speedCtrlSlider.setValue(0);
			for (IReplayControlPanelObserver o : observers)
			{
				o.onSetSpeed(1);
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
	
	
	@Override
	public void onPositionChanged(final int position)
	{
		sliderManuelSet = true;
		slider.setValue(position);
	}
}
