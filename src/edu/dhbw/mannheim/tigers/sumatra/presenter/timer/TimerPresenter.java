/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.timer;

import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ITimerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.view.timer.TimerChartPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.timer.TimerPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Presenter for the Timer-GUI
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TimerPresenter implements IModuliStateObserver, ITimerObserver, ISumatraViewPresenter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log					= Logger.getLogger(TimerPresenter.class.getName());
	
	private static final long			TIMER_UPDATE_RATE	= 100;
	
	private final TimerPanel			timerPanel;
	private final TimerChartPanel		chartPanel;
	private SumatraTimer					timer;
	private ScheduledExecutorService	service;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TimerPresenter()
	{
		timerPanel = new TimerPanel();
		chartPanel = timerPanel.getChartPanel();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
					timer.addObserver(this);
					
				} catch (final ModuleNotFoundException err)
				{
					log.error("Timer Module not found!", err);
				}
				
				service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("TimerUpdate"));
				service.scheduleAtFixedRate(new Runner(), 0, TIMER_UPDATE_RATE, TimeUnit.MILLISECONDS);
				
				break;
			}
			
			case RESOLVED:
			{
				if (service != null)
				{
					service.shutdown();
					try
					{
						service.awaitTermination(500, TimeUnit.MILLISECONDS);
					} catch (InterruptedException err)
					{
						log.error("Interrupted while waiting for shutdown of TimerRunner", err);
					}
				}
				if (timer != null)
				{
					timer.removeObserver(this);
				}
				
				chartPanel.clearChart();
				
				break;
			}
			default:
		}
		
	}
	
	
	@Override
	public void onNewTimerInfo(final TimerInfo info)
	{
		chartPanel.onNewTimerInfo(info);
	}
	
	
	private class Runner implements Runnable
	{
		
		@Override
		public void run()
		{
			try
			{
				TimerInfo info = timer.getTimerInfo();
				chartPanel.onNewTimerInfo(info);
			} catch (Exception err)
			{
				log.error("Error in TimerRunner", err);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public Component getComponent()
	{
		return timerPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return timerPanel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
}
