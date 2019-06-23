/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref.view.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;

import net.miginfocom.swing.MigLayout;
import edu.tigers.autoref.view.main.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.sumatra.components.BasePanel;


/**
 * @author Lukas Magel
 */
public class StartStopPanel extends BasePanel<IStartStopPanelObserver>
{
	/**
	 * @author Lukas Magel
	 */
	public interface IStartStopPanelObserver
	{
		/**
		 * 
		 */
		void onStartButtonPressed();
		
		
		/**
		 * 
		 */
		void onStopButtonPressed();
		
		
		/**
		 * 
		 */
		void onPauseButtonPressed();
		
		
		/**
		 * 
		 */
		void onResumeButtonPressed();
		
	}
	
	/**  */
	private static final long			serialVersionUID	= 1L;
	
	private JButton						startButton			= null;
	private JButton						stopButton			= null;
	private JButton						pauseButton			= null;
	private JButton						resumeButton		= null;
	private JComboBox<AutoRefMode>	refModeBox			= null;
	
	
	/**
	 * 
	 */
	public StartStopPanel()
	{
		setLayout(new MigLayout("", "[16%][16%][16%][16%][16%][16%]", ""));
		
		startButton = new JButton("Start");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				informObserver(obs -> obs.onStartButtonPressed());
			}
		});
		
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				informObserver(obs -> obs.onStopButtonPressed());
			}
		});
		
		pauseButton = new JButton("Pause");
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(e -> informObserver(obs -> obs.onPauseButtonPressed()));
		
		resumeButton = new JButton("Resume");
		resumeButton.setEnabled(false);
		resumeButton.addActionListener(e -> informObserver(obs -> obs.onResumeButtonPressed()));
		
		
		refModeBox = new JComboBox<>(AutoRefMode.values());
		
		add(refModeBox, "span 2, grow");
		add(startButton, "span 2, grow");
		add(stopButton, "span 2, grow, wrap");
		add(pauseButton, "span 3, grow");
		add(resumeButton, "span 3, grow");
		
		setBorder(BorderFactory.createTitledBorder("Start/Stop"));
	}
	
	
	/**
	 * @param enabled
	 */
	public void setModeBoxEnabled(final boolean enabled)
	{
		refModeBox.setEnabled(enabled);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setStartButtonEnabled(final boolean enabled)
	{
		startButton.setEnabled(enabled);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setStopButtonEnabled(final boolean enabled)
	{
		stopButton.setEnabled(enabled);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setPauseButtonEnabled(final boolean enabled)
	{
		pauseButton.setEnabled(enabled);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setResumeButtonEnabled(final boolean enabled)
	{
		resumeButton.setEnabled(enabled);
	}
	
	
	/**
	 * @return
	 */
	public AutoRefMode getModeSetting()
	{
		return (AutoRefMode) refModeBox.getSelectedItem();
	}
	
	
	@Override
	public void setPanelEnabled(final boolean enabled)
	{
		if (enabled == false)
		{
			Arrays.asList(startButton, pauseButton, resumeButton, stopButton, refModeBox)
					.forEach(comp -> comp.setEnabled(false));
		}
	}
	
	
	/**
	 * @param state
	 */
	public void setState(final AutoRefState state)
	{
		boolean startEnabled = false;
		boolean stopEnabled = false;
		boolean pauseEnabled = false;
		switch (state)
		{
			case RUNNING:
				stopEnabled = true;
				pauseEnabled = true;
				break;
			case PAUSED:
				stopEnabled = true;
				break;
			case STOPPED:
				startEnabled = true;
				break;
			default:
				break;
		}
		
		setStartButtonEnabled(startEnabled);
		setStopButtonEnabled(stopEnabled);
		setModeBoxEnabled(startEnabled);
		setPauseButtonEnabled(stopEnabled && pauseEnabled);
		setResumeButtonEnabled(stopEnabled && !pauseEnabled);
	}
}
