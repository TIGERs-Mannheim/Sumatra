/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.10.2010
 * Author(s): Marcel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.wpcenter;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals.AnalysisCharts;
import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals.IAnalysisChartsObserver;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Presenter for the Timer-GUI
 * 
 * @author Marcel
 * 
 */
public class WPChartPresenter implements IModuliStateObserver, IWorldPredictorObserver, ICamDetnObserver, IAnalysisChartsObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final int           pufferSize = 100;
	private final ATrackedObject frames[] = new ATrackedObject[pufferSize];
	private final long      frame_times[] = new long[pufferSize];
	private int   frame_first = 0;
	private int   frame_last  = 0;
	
	private final int fpsIntervall = 15;
	private int fps_up_counter = fpsIntervall;
	private int fps_in_counter = fpsIntervall;
	private int fps_out_counter = fpsIntervall;
	private long fps_up_tmp = 0;
	private long fps_in_tmp = 0;
	private long fps_out_tmp = 0;
		
	private final Logger				log	= Logger.getLogger(getClass());
	

	private final SumatraModel		model	= SumatraModel.getInstance();
	
	private AnalysisCharts chart = null;
	
	
	private int targetId   = 0;
	
	AWorldPredictor wp = null;
	ACam cam = null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public WPChartPresenter()
	{
		chart = new AnalysisCharts();
		chart.addObserver(this);
		ModuliStateAdapter.getInstance().addObserver(this);
		frame_times[0] = 0;
		
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
					wp = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
					wp.addObserver(this);
					cam = (ACam) model.getModule(ACam.MODULE_ID);
					cam.addCamDetectionObserver(this);
					
				} catch (ModuleNotFoundException err)
				{
					log.error("Timer Module not found!");
				}
				break;
			}
				
			default:
			{
				if (wp != null)
				{
					wp.removeObserver(this);
					wp = null;
				}
				
				if (cam != null)
				{
					cam.removeCamDetectionObserver(this);
					cam = null;
				}
				
				chart.clearChart();
				
				break;
			}
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public JPanel getChart()
	{
		return chart;
	}




	@Override
	public void onSetId(int id)
	{
		targetId = id;
		frame_first = 0;
		frame_last = 0;
		chart.clearChart();		
	}


	@Override
	public void onShowA()
	{
		chart.setVisibleA(!chart.isVisibleA());
	}


	@Override
	public void onShowAbs()
	{
		chart.setVisibleAbs(!chart.isVisibleAbs());
		
	}


	@Override
	public void onShowX()
	{
		chart.setVisibleX(!chart.isVisibleX());
		
	}


	@Override
	public void onShowY()
	{
		chart.setVisibleY(!chart.isVisibleY());
		
	}
	
	private void handleUpdate(CamDetectionFrame df, int before, int after)
	{
		long time = df.tCapture;
//		this.chart.setPredDelta(time/1000000.0f);
		float infl_before = ((float) (time -frame_times[before]))/(frame_times[after]-frame_times[before]);
		float infl_after = 1- infl_before;
		
		if (targetId == 0)
		{
			if (df.balls.size() > 0)
			{
				if (++fps_up_counter > fpsIntervall)
				{
					chart.setFpsUp((fpsIntervall*1000000000.0f)/(df.tCapture - fps_up_tmp));
					fps_up_counter = 1;
					fps_up_tmp = df.tCapture;
				}
				
				CamBall ball = df.balls.get(0);
				float xError = (infl_before*((TrackedBall) frames[before]).pos.x + (infl_after*((TrackedBall) frames[after]).pos.x))-ball.pos.x;
				float yError = (infl_before*((TrackedBall) frames[before]).pos.y + (infl_after*((TrackedBall) frames[after]).pos.y))-ball.pos.y;
				float absError = (float) Math.sqrt(xError*xError+yError*yError);
				chart.updateErrors(time, xError, yError, 0.0f, absError);
			}
			  
		}
		else
		{
			CamRobot bot = null;
			for (CamRobot tmpBot: df.robotsEnemies)
			{
				if (tmpBot.robotID == targetId)
				{
					bot = tmpBot;
					break;
				}
			}
			for (CamRobot tmpBot: df.robotsTigers)
			{
				if (tmpBot.robotID == targetId)
				{
					bot = tmpBot;
					break;
				}
			}
			if (bot != null)
			{
				if (++fps_up_counter > fpsIntervall)
				{
					chart.setFpsUp((fpsIntervall*1000000000.0f)/(df.tCapture - fps_up_tmp));
					fps_up_counter = 1;
					fps_up_tmp = df.tCapture;
				}
				TrackedBot bot1 = (TrackedBot) frames[before];
				TrackedBot bot2 = (TrackedBot) frames[after];
				float xError = infl_before*bot1.pos.x + infl_after*bot2.pos.x-bot.pos.x;
				float yError = infl_before*bot1.pos.y + infl_after*bot2.pos.y-bot.pos.y;
				float aError = 0;
				if (Math.abs(bot1.angle - bot2.angle) > Math.PI)
				{
					if (bot1.angle > bot2.angle)
					{
						aError = (float) (infl_before*(bot1.angle-2*Math.PI) + infl_after*bot2.angle - bot.orientation);
					}
					else
					{
						aError = (float) (infl_before*(bot1.angle+2*Math.PI) + infl_after*bot2.angle - bot.orientation);
					}
				}
				if (aError > Math.PI)
					aError -= 2*Math.PI;
				else
					if (aError < -Math.PI)
					{
						aError += 2*Math.PI;
					}
				float absError = (float) Math.sqrt(xError*xError+yError*yError);
				chart.updateErrors(time, xError, yError, aError, absError);				
			}
		}
	}


	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{


		
		if (++fps_out_counter > fpsIntervall)
		{
			chart.setFpsOut((fpsIntervall*1000000000.0f)/(wf.time - fps_out_tmp));
			fps_out_counter = 1;
			fps_out_tmp = wf.time;
		}
		
		
		if (wf.time < frame_times[frame_last])
		{
			return;
		}
		
		if (targetId == 0)
		{
			
			if (wf.ball != null)
			{
				frames[frame_last] = wf.ball;
				frame_times[frame_last] = wf.time;
				frame_last = (++frame_last)%pufferSize;
				if (frame_last == frame_first)
				{
					frame_last = (frame_last-1+pufferSize)%pufferSize;
				}
				chart.updateWF(wf.time, wf.ball.pos.x, wf.ball.pos.y, 0.0f);
			}
		}
		else
		{
			TrackedBot bot;
			bot = wf.foeBots.get(targetId);
			if (bot == null)
			{
				bot = wf.tigerBots.get(targetId);
			}
			if (bot != null)
			{
				frames[frame_last] = bot;
				frame_times[frame_last] = wf.time;
				frame_last = (++frame_last)%pufferSize;
				if (frame_last == frame_first)
				{
					frame_last = (frame_last-1+pufferSize)%pufferSize;
				}
				chart.updateWF(wf.time, bot.pos.x, bot.pos.y, bot.angle);
				return;
			}
		}
		

	}


	@Override
	public void update(ICamDetnObservable observable, CamDetectionFrame event)
	{
		if (++fps_in_counter > fpsIntervall)
		{
			chart.setFpsIn((fpsIntervall*1000000000.0f)/(event.tCapture - fps_in_tmp));
			fps_in_counter = 1;
			fps_in_tmp = event.tCapture;
		}
		
		
		int i = frame_first;
		long time = event.tCapture;
		while (true)
		{
			if (frame_times[i] > time)
			{
				if (i != frame_first)
				{
					handleUpdate(event, (i-1+pufferSize)%pufferSize, i);
					
					if ((i-1+pufferSize)%pufferSize != frame_first)
						frame_first = (i-2+pufferSize)%pufferSize;
					
				}
				break;
			}
			if (i == frame_last)
			{
				break;
			}
			i = (++i)%pufferSize;
		}

		
		if (targetId == 0)
		{
			
			if (event.balls.size() > 0)
			{
				CamBall ball = event.balls.get(0);
				chart.updateCF(event.tCapture, ball.pos.x, ball.pos.y, 0.0f);
			}
		}
		else
		{
			for (CamRobot bot: event.robotsEnemies)
			{
				if (bot.robotID == targetId)
				{
					chart.updateCF(event.tCapture, bot.pos.x, bot.pos.y, bot.orientation);
					return;
				}
			}
			for (CamRobot bot: event.robotsTigers)
			{
				if (bot.robotID == targetId)
				{
					chart.updateCF(event.tCapture, bot.pos.x, bot.pos.y, bot.orientation);
					return;
				}
			}
		}
	}
}
