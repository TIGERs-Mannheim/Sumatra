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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.view.timer.TimerChartPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.timer.TimerPanel;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.timer.ITimerObserver;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.timer.TimerInfo;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Presenter for the Timer-GUI
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TimerPresenter extends ASumatraViewPresenter implements ITimerObserver
{
	// Logger
	private static final Logger	log					= Logger.getLogger(TimerPresenter.class.getName());
																	
	private static final int		TIMER_UPDATE_RATE	= 100;
																	
	private final TimerPanel		timerPanel;
	private final TimerChartPanel	chartPanel;
	private SumatraTimer				timer;
	private Timer						updateTimer;
											
											
	/**
	 * 
	 */
	public TimerPresenter()
	{
		timerPanel = new TimerPanel();
		chartPanel = timerPanel.getChartPanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
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
				
				updateTimer = new Timer(TIMER_UPDATE_RATE, new Runner());
				updateTimer.start();
				
				break;
			}
			
			case RESOLVED:
			{
				if (updateTimer != null)
				{
					updateTimer.stop();
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
	
	
	private class Runner implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
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
}
