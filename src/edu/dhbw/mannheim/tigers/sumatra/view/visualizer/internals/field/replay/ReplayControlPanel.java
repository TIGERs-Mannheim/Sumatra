/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * 
 */
public class ReplayControlPanel extends JPanel implements IReplayPositionObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long								serialVersionUID	= 1L;
	private final List<IReplayControlPanelObserver>	observers			= new CopyOnWriteArrayList<IReplayControlPanelObserver>();
	private final JSlider									slider;
	
	
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
		
		JToggleButton btnPlay = new JToggleButton("Play");
		btnPlay.setSelected(true);
		btnPlay.addActionListener(new PlayButtonListener());
		JButton btnFaster = new JButton("Faster");
		btnFaster.addActionListener(new FasterButtonListener());
		JButton btnSlower = new JButton("Slower");
		btnSlower.addActionListener(new SlowerButtonListener());
		
		add(slider);
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
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof JToggleButton)
			{
				JToggleButton src = (JToggleButton) e.getSource();
				for (IReplayControlPanelObserver o : observers)
				{
					o.onPlayStateChanged(src.isSelected());
					if (src.isSelected())
					{
						src.setText("Play");
					} else
					{
						src.setText("Pause");
					}
				}
			}
		}
		
	}
	
	private class FasterButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
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
		public void actionPerformed(ActionEvent e)
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
		public void stateChanged(ChangeEvent e)
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
		
		
	}
	
	
	@Override
	public void onPositionChanged(int position)
	{
		slider.setValue(position);
	}
}
