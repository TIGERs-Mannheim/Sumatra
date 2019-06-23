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
import edu.dhbw.mannheim.tigers.sumatra.model.data.TimerInfo;
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
 * 
 */
public class TimerPresenter implements IModuliStateObserver, ITimerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger				log	= Logger.getLogger(getClass());
	

	private final SumatraModel		model	= SumatraModel.getInstance();
	private ATimer						timer;
	
	private final TimerPanel		timerPanel;
	private final TimerChartPanel	chartPanel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
					timer = (ATimer) model.getModule(ATimer.MODULE_ID);
					timer.addObserver(this);
					
				} catch (ModuleNotFoundException err)
				{
					log.error("Timer Module not found!");
				}
				break;
			}
				
			default:
			{
				if (timer != null)
				{
					timer.removeObserver(this);
				}
				
				chartPanel.clearChart();
				
				break;
			}
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
	public ISumatraView getView()
	{
		return timerPanel;
	}
}
