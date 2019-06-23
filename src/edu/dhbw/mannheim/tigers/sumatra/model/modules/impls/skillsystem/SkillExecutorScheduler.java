/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 26, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.EBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IdleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;


/**
 * The SkillExecuter will execute skills for all bots in one thread
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SkillExecutorScheduler
{
	private static final Logger							log							= Logger
																											.getLogger(SkillExecutorScheduler.class
																													.getName());
	private final WorldFrameConsumer						wfConsumer					= new WorldFrameConsumer();
	
	private SumatraTimer										timer							= null;
	private Thread												thread;
	private final BlockingDeque<WorldFrameWrapper>	freshWorldFrames			= new LinkedBlockingDeque<WorldFrameWrapper>(
																											1);
	private WorldFrameWrapper								latestWorldFrameWrapper	= WorldFrameWrapper.createDefault(0);
	
	private Map<BotID, SkillExecutor>					executors					= new ConcurrentHashMap<BotID, SkillExecutor>(
																											12);
	private BaseStation										baseStation					= null;
	
	
	/**
	 * 
	 */
	public SkillExecutorScheduler()
	{
		
	}
	
	
	/**
	 */
	public void start()
	{
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find timer", err);
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumerHungry(wfConsumer);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find worldpredictor", err);
		}
		
		
		try
		{
			ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			baseStation = botManager.getBaseStations().get(EBaseStation.PRIMARY);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find botManager", err);
		}
		
		thread = new Thread(new Runner(), "SkillExecuter");
		thread.start();
	}
	
	
	/**
	 */
	public void stop()
	{
		thread.interrupt();
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumerHungry(wfConsumer);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find worldpredictor", err);
		}
	}
	
	
	/**
	 * Enqueue skill for execution
	 * 
	 * @param skill
	 * @param botId
	 */
	public void execute(final ISkill skill, final BotID botId)
	{
		SkillExecutor se = executors.get(botId);
		if (se == null)
		{
			log.warn("Tried to execute a skill for a non existing bot");
		} else
		{
			se.setNewSkill(skill);
		}
	}
	
	
	/**
	 */
	public void resetAll()
	{
		for (SkillExecutor se : executors.values())
		{
			se.setNewSkill(new IdleSkill());
		}
	}
	
	
	/**
	 * @return
	 */
	public List<ISkill> getCurrentSkills()
	{
		List<ISkill> skills = new ArrayList<ISkill>(executors.size());
		for (SkillExecutor se : executors.values())
		{
			skills.add(se.getCurrentSkill());
		}
		return skills;
	}
	
	
	/**
	 * @param observer
	 * @param botId
	 */
	public void addObserver(final ISkillExecutorObserver observer, final BotID botId)
	{
		SkillExecutor se = executors.get(botId);
		if (se == null)
		{
			log.warn("No SkillExecuter for botId " + botId);
		} else
		{
			se.addObserver(observer);
		}
	}
	
	
	/**
	 * @param observer
	 * @param botId
	 */
	public void removeObserver(final ISkillExecutorObserver observer, final BotID botId)
	{
		SkillExecutor se = executors.get(botId);
		if (se == null)
		{
			log.warn("No SkillExecuter for botId " + botId);
		} else
		{
			se.removeObserver(observer);
		}
	}
	
	
	/**
	 * @param botId
	 * @param skillExecutor
	 */
	public void addSkillExecutor(final BotID botId, final SkillExecutor skillExecutor)
	{
		executors.put(botId, skillExecutor);
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	public SkillExecutor removeSkillExecutor(final BotID botId)
	{
		return executors.remove(botId);
	}
	
	
	/**
	 * Process all skill executors
	 * 
	 * @param wf
	 */
	public void process(final WorldFrameWrapper wf)
	{
		for (SkillExecutor se : executors.values())
		{
			se.update(wf);
		}
	}
	
	
	private class WorldFrameConsumer implements IWorldFrameConsumer
	{
		@Override
		public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
		{
			freshWorldFrames.clear();
			freshWorldFrames.addLast(wfWrapper);
			latestWorldFrameWrapper = wfWrapper;
		}
		
		
		@Override
		public void onStop()
		{
		}
	}
	
	
	private class Runner implements Runnable
	{
		
		@Override
		public void run()
		{
			while (!Thread.interrupted())
			{
				try
				{
					WorldFrameWrapper wf = latestWorldFrameWrapper;
					int updateRate = 100;
					if (baseStation != null)
					{
						updateRate = baseStation.getUpdateRate() - 10;
						if (updateRate < 1)
						{
							updateRate = 100;
						}
					}
					
					long tSleep = (long) (1e9 / updateRate);
					
					long tStart = System.nanoTime();
					long frameId = wf.getSimpleWorldFrame().getId();
					timer.start(ETimable.SKILLS, frameId);
					process(wf);
					timer.stop(ETimable.SKILLS, frameId);
					long tStop = System.nanoTime();
					tSleep -= tStop - tStart;
					ThreadUtil.parkNanosSafe(tSleep);
				} catch (Exception err)
				{
					log.error("Error in SkillExecuter!", err);
				}
			}
		}
	}
	
	
	/**
	 * @return the executors
	 */
	public final Map<BotID, SkillExecutor> getExecutors()
	{
		return executors;
	}
}
