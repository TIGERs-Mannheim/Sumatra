/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.multiteammessage.MultiTeamMessageHandler;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.timer.DummyTimer;
import edu.tigers.sumatra.timer.ITimer;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * The AI manager is responsible for executing the AI in its own thread in the desired speed
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiManager
{
	private static final Logger log = Logger.getLogger(AiManager.class.getName());
	private static final double MIN_DT = 0.015;
	
	private final BlockingDeque<WorldFrameWrapper> freshWorldFrames = new LinkedBlockingDeque<>(1);
	private final AiProcessor aiProcessor = new AiProcessor();
	private final AAgent agent;
	private final Ai ai;
	private final String aiName;
	private final MultiTeamMessageHandler multiTeamMessageHandler;
	
	private ExecutorService executor;
	private ITimer timer = new DummyTimer();
	private long tLast = 0;
	
	
	/**
	 * @param agent
	 * @param aiTeam
	 * @param skillSystem
	 */
	public AiManager(final AAgent agent, final EAiTeam aiTeam, final ASkillSystem skillSystem)
	{
		this.agent = agent;
		ai = new Ai(aiTeam, skillSystem);
		aiName = "AI_" + ai.getAiTeam();
		multiTeamMessageHandler = new MultiTeamMessageHandler(aiTeam);
	}
	
	
	/**
	 * Start the AI
	 */
	public void start()
	{
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (final ModuleNotFoundException err)
		{
			log.warn("No timer module found.", err);
		}
		
		assert executor == null;
		executor = Executors.newSingleThreadExecutor();
		executor.execute(aiProcessor);
	}
	
	
	/**
	 * Stop the AI
	 */
	public void stop()
	{
		if (multiTeamMessageHandler.isActive())
		{
			multiTeamMessageHandler.stop();
		}
		aiProcessor.running = false;
		executor.shutdownNow();
		
		try
		{
			Validate.isTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}
		
		executor = null;
	}
	
	
	/**
	 * Enable multi team mode
	 * 
	 * @param enabled
	 */
	public void setMixedTeamEnabled(boolean enabled)
	{
		if (enabled && !multiTeamMessageHandler.isActive())
		{
			multiTeamMessageHandler.start();
		} else if (!enabled && multiTeamMessageHandler.isActive())
		{
			multiTeamMessageHandler.stop();
		}
	}
	
	
	/**
	 * True if this AI is running.
	 * 
	 * @return
	 */
	public boolean isRunning()
	{
		return executor != null;
	}
	
	
	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		ai.changeMode(mode);
	}
	
	
	public BlockingDeque<WorldFrameWrapper> getFreshWorldFrames()
	{
		return freshWorldFrames;
	}
	
	
	public Ai getAi()
	{
		return ai;
	}
	
	private class AiProcessor implements Runnable
	{
		boolean running = true;
		
		
		@Override
		public void run()
		{
			Thread.currentThread().setName(aiName);
			while (running)
			{
				try
				{
					process();
				} catch (InterruptedException err)
				{
					Thread.currentThread().interrupt();
				}
			}
			ai.stop();
			agent.notifyAIStopped(ai.getAiTeam());
		}
		
		
		private void process() throws InterruptedException
		{
			WorldFrameWrapper wfw = freshWorldFrames.takeLast();
			if (wfw == null)
			{
				return;
			}
			
			long tNow = wfw.getSimpleWorldFrame().getTimestamp();
			double dt = Math.abs(tNow - tLast) / 1e9;
			if (dt < MIN_DT)
			{
				return;
			}
			tLast = tNow;
			
			long id = wfw.getSimpleWorldFrame().getId();
			
			timer.start(aiName, id);
			
			AIInfoFrame frame = ai.processWorldFrame(wfw, multiTeamMessageHandler.getMultiTeamMessage());
			if (frame != null)
			{
				try
				{
					agent.notifyNewAIInfoFrame(frame);
					VisualizationFrame visFrame = new VisualizationFrame(frame);
					agent.notifyNewAIInfoFrameVisualize(visFrame);
					if (multiTeamMessageHandler.isActive())
					{
						multiTeamMessageHandler.sendTeamPlan(frame.getTacticalField().getMultiTeamPlan());
					}
				} catch (Throwable err)
				{
					log.error("Error during AI frame publishing", err);
				}
			}
			timer.stop(aiName, id);
		}
	}
}
