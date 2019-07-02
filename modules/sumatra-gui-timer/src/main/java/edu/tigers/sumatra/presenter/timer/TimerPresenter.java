/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.timer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.timer.ITimerObserver;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.timer.TimerInfo;
import edu.tigers.sumatra.view.timer.TimerChartPanel;
import edu.tigers.sumatra.view.timer.TimerPanel;
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
	private static final Logger log = Logger.getLogger(TimerPresenter.class.getName());
	
	private static final int TIMER_UPDATE_RATE = 100;
	
	private final TimerPanel timerPanel;
	private final TimerChartPanel chartPanel;
	private SumatraTimer timer;
	private Timer updateTimer;
	
	
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
				final Optional<SumatraTimer> timerOpt = SumatraModel.getInstance().getModuleOpt(SumatraTimer.class);
				if (timerOpt.isPresent())
				{
					timer = timerOpt.get();
					timer.addObserver(this);
					
					updateTimer = new Timer(TIMER_UPDATE_RATE, new Runner());
					updateTimer.start();
				}
				break;
			
			case RESOLVED:
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
			if (timer == null)
			{
				return;
			}
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
