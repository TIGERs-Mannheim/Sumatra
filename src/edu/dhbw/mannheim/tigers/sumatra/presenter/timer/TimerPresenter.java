/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.timer;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ITimerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.timer.TimerChartPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.timer.TimerPanel;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Presenter for the Timer-GUI
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TimerPresenter implements IModuliStateObserver, ITimerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(TimerPresenter.class.getName());
	
	private final TimerPanel		timerPanel;
	private final TimerChartPanel	chartPanel;
	
	
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
		
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					ATimer timer = (ATimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
					timer.addObserver(this);
					
				} catch (final ModuleNotFoundException err)
				{
					log.error("Timer Module not found!");
				}
				break;
			}
			
			case RESOLVED:
			{
				try
				{
					ATimer timer = (ATimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
					timer.removeObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Timer Module not found!");
				}
				
				chartPanel.clearChart();
				
				break;
			}
			default:
		}
		
	}
	
	
	@Override
	public void onNewTimerInfo(TimerInfo info)
	{
		chartPanel.onNewTimerInfo(info);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public ISumatraView getView()
	{
		return timerPanel;
	}
}
