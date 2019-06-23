/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.presenter;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.autoref.model.ballspeed.BallSpeedModel;
import edu.tigers.autoref.view.ballspeed.BallSpeedPanel;
import edu.tigers.autoref.view.ballspeed.IBallSpeedPanelListener;
import edu.tigers.autoref.view.generic.FixedTimeRangeChartPanel;
import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter class that displays ball velocity data in a {@link FixedTimeRangeChartPanel} instance to create an
 * oscilloscope like effect where the graph line chases its own tail. To achieve this effect the chart only displays a
 * fixed amount of data points. The presenter calculates sets the buffer size of the chart accordingly to always have
 * the chart display enough dots to fill about 90 percent of the width.
 * The plot can be paused by the user. This would normally cause gaps in the plot where it was paused. These gaps also
 * distort the oscilloscope effect since the size of the data point buffer of the chart is set based on the assumption
 * that the entire x range is filled with data points. To circumvent this issue this class does not use the timestamps
 * of the world frames directly but maintains its own timestamp value that is incremented with the time delta of two
 * consecutive frames. To avoid gaps the timestamp value is not updated when the plot is paused. Because of this new
 * data points are displayed directly after the last points before the pause.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedPresenter implements ISumatraViewPresenter, IWorldFrameObserver, IModuliStateObserver,
		IBallSpeedPanelListener, ActionListener
{
	private enum PauseState
	{
		/** The chart has been paused manually by the user */
		MANUAL,
		/** The chart has been paused through the auto pause setting if the gamestate is not running */
		AUTO,
		/** The chart is running */
		RUNNING
	}
	
	/** The period in ms at the end of which the chart is updated */
	private static final int	chartUpdatePeriod		= 50;
	
	/** The absolute time range displayed in the chart in seconds */
	private int						timeRange				= 20;
	private boolean				pauseWhenNotRunning	= false;
	private boolean				pauseRequested			= false;
	private boolean				resumeRequested		= false;
	private PauseState			chartState				= PauseState.RUNNING;
	
	private long					curTime					= 0L;
	private BallSpeedModel		model						= new BallSpeedModel();
	
	private Timer					chartTimer;
	private BallSpeedPanel		panel;
	
	
	/**
	 * 
	 */
	public BallSpeedPresenter()
	{
		panel = new BallSpeedPanel(getTimeRange(), TimeUnit.MILLISECONDS.toNanos(chartUpdatePeriod));
		panel.addObserver(this);
		panel.setMaxBallVelocityLine(AutoRefConfig.getMaxBallVelocity());
		
		chartTimer = new Timer(chartUpdatePeriod, this);
		chartTimer.setDelay(chartUpdatePeriod);
		
		ConfigRegistration.registerConfigurableCallback("autoreferee", new IConfigObserver()
		{
			@Override
			public void afterApply(final IConfigClient configClient)
			{
				panel.setMaxBallVelocityLine(AutoRefConfig.getMaxBallVelocity());
			}
		});
	}
	
	
	@Override
	public Component getComponent()
	{
		return panel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}
	
	
	/**
	 * @return time range in naoseconds
	 */
	private long getTimeRange()
	{
		return TimeUnit.SECONDS.toNanos(timeRange);
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		Optional<AWorldPredictor> optPredictor = getPredictor();
		
		if (state == ModulesState.ACTIVE)
		{
			optPredictor.ifPresent(predictor -> {
				predictor.addWorldFrameConsumer(this);
				chartTimer.start();
			});
		} else if (state == ModulesState.RESOLVED)
		{
			chartTimer.stop();
			optPredictor.ifPresent(predictor -> predictor.removeWorldFrameConsumer(this));
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		EventQueue.invokeLater(() -> model.update(wFrameWrapper));
	}
	
	
	@Override
	public void actionPerformed(final ActionEvent e)
	{
		updateChart();
		model.reset();
	}
	
	
	private void updateChart()
	{
		updateChartState();
		
		if (chartState == PauseState.RUNNING)
		{
			curTime += TimeUnit.MILLISECONDS.toNanos(chartUpdatePeriod);
			panel.addPoint(curTime, model.getLastBallSpeed());
		}
	}
	
	
	/**
	 * Updates the state of the chart according to the gamestate and requests from the user.
	 * The chart can be in a RUNNING, MANUAL (manually stopped by the user) or AUTO (automatically stopped
	 * by the "Pause When Not Running" feature).
	 */
	private void updateChartState()
	{
		if (model.hasGameStateChanged() && pauseWhenNotRunning && (chartState != PauseState.MANUAL))
		{
			/*
			 * The auto pause feature is activated, a gamestate change has been detected, and the chart is not in a
			 * manually paused state. This means that depending on the current state the chart is either put in auto pause
			 * or running state
			 */
			if (model.getLastState() == EGameStateNeutral.RUNNING)
			{
				chartState = PauseState.RUNNING;
			} else
			{
				chartState = PauseState.AUTO;
			}
		}
		
		/*
		 * A manualy pause request will override the automatic pause/resume mechanism and will cause the chart to be
		 * paused until the resume button is pressed
		 */
		if (pauseRequested)
		{
			chartState = PauseState.MANUAL;
			pauseRequested = false;
		}
		/*
		 * The resume request will put the chart back into the running state no matter if it was manually or automatically
		 * paused.
		 */
		if (resumeRequested)
		{
			chartState = PauseState.RUNNING;
			resumeRequested = false;
		}
	}
	
	
	private Optional<AWorldPredictor> getPredictor()
	{
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			return Optional.of(predictor);
		} catch (ModuleNotFoundException e)
		{
		}
		return Optional.empty();
	}
	
	
	@Override
	public void pauseButtonPressed()
	{
		pauseRequested = true;
	}
	
	
	@Override
	public void resumeButtonPressed()
	{
		resumeRequested = true;
	}
	
	
	@Override
	public void stopChartValueChanged(final boolean value)
	{
		pauseWhenNotRunning = value;
		
		/*
		 * The updateChartState() method only triggers on state transitions that occur after the pauseWhenNotRunning
		 * variable has been altered. To also stop/restart the chart if the pauseWhenNotRunning feature is first
		 * activated/deactivated the state update is performed directly inside the callback
		 */
		if (pauseWhenNotRunning)
		{
			if ((model.getLastState() != EGameStateNeutral.RUNNING) && (chartState == PauseState.RUNNING))
			{
				chartState = PauseState.AUTO;
			}
		} else
		{
			if (chartState == PauseState.AUTO)
			{
				chartState = PauseState.RUNNING;
			}
		}
	}
	
	
	@Override
	public void timeRangeSliderValueChanged(final int value)
	{
		timeRange = value;
		panel.setTimeRange(getTimeRange());
		curTime = 0;
	}
	
	
}
