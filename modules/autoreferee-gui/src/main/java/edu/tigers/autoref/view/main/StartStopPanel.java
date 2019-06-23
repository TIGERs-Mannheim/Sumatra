/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import java.util.Arrays;

import javax.swing.*;

import edu.tigers.autoref.view.main.IStartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.module.AutoRefState;
import edu.tigers.sumatra.components.BasePanel;
import net.miginfocom.swing.MigLayout;


/**
 * @author Lukas Magel
 */
public class StartStopPanel extends BasePanel<IStartStopPanelObserver> implements IStartStopPanel
{
	
	/**  */
	private static final long			serialVersionUID	= 1L;
	
	private JButton						startButton			= null;
	private JButton						stopButton			= null;
	private JButton						pauseButton			= null;
	private JButton						resumeButton		= null;
	private JComboBox<AutoRefMode>	refModeBox			= null;
	
	
	/**
	 * Create new instance
	 */
	public StartStopPanel()
	{
		setLayout(new MigLayout("", "[16%][16%][16%][16%][16%][16%]", ""));
		
		startButton = new JButton("Start");
		startButton.setEnabled(false);
		startButton.addActionListener(e -> informObserver(IStartStopPanelObserver::onStartButtonPressed));
		
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(e -> informObserver(IStartStopPanelObserver::onStopButtonPressed));
		
		pauseButton = new JButton("Pause");
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(e -> informObserver(IStartStopPanelObserver::onPauseButtonPressed));
		
		resumeButton = new JButton("Resume");
		resumeButton.setEnabled(false);
		resumeButton.addActionListener(e -> informObserver(IStartStopPanelObserver::onResumeButtonPressed));
		
		
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
	
	
	@Override
	public AutoRefMode getModeSetting()
	{
		return (AutoRefMode) refModeBox.getSelectedItem();
	}
	
	
	@Override
	public void setPanelEnabled(final boolean enabled)
	{
		if (!enabled)
		{
			Arrays.asList(startButton, pauseButton, resumeButton, stopButton, refModeBox)
					.forEach(comp -> comp.setEnabled(false));
		}
	}
	
	
	/**
	 * @param state
	 */
	@Override
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
