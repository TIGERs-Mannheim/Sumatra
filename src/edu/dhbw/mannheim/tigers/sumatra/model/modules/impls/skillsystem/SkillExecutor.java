/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.NormalStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;


/**
 * Skill executer with nano second precision.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class SkillExecutor implements Runnable, IWorldPredictorObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger						log							= Logger.getLogger(SkillExecutor.class
																										.getName());
	
	private static final long							NO_SKILL_TIMEOUT_NS		= TimeUnit.MILLISECONDS.toNanos(500);
	
	private AMoveSkill									skill							= null;
	
	private final BotID									botId;
	
	/** [ns] */
	private final long									period;
	
	/** Volatile because it is written from the outside */
	private volatile WorldFrame						latestWorldFrame			= null;
	private Sisyphus										latestSisyphus				= null;
	private final List<ISkillExecutorObserver>	observers					= new ArrayList<ISkillExecutorObserver>();
	
	private final BlockingDeque<AMoveSkill>		newSkills					= new LinkedBlockingDeque<AMoveSkill>(1);
	
	private long											timeLastCompletedSkill	= 0;
	private boolean										stopSent						= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param botId
	 * @param period [us]
	 */
	public SkillExecutor(BotID botId, long period)
	{
		this.botId = botId;
		this.period = TimeUnit.MICROSECONDS.toNanos(period);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param observer
	 */
	public void addObserver(ISkillExecutorObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(ISkillExecutorObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	@Override
	public synchronized void run()
	{
		try
		{
			final WorldFrame worldFrame = latestWorldFrame;
			
			if (worldFrame == null)
			{
				return;
			}
			final TrackedTigerBot bot = worldFrame.tigerBotsVisible.getWithNull(botId);
			
			
			if (bot == null)
			{
				resetSkills();
				return;
			}
			AMoveSkill newSkill;
			synchronized (newSkills)
			{
				newSkill = newSkills.poll();
			}
			if ((skill != null) || (newSkill != null))
			{
				process(bot, worldFrame, newSkill);
				if ((skill != null) && (skill.getSkillName() != ESkillName.IMMEDIATE_STOP))
				{
					stopSent = false;
				}
			} else if (!stopSent && ((System.nanoTime() - timeLastCompletedSkill) > NO_SKILL_TIMEOUT_NS))
			{
				sentStop(worldFrame);
			}
		} catch (Throwable err)
		{
			log.error("Exception in Skillexecuter: " + err.getMessage(), err);
			resetSkills();
		}
	}
	
	
	private void process(TrackedTigerBot bot, WorldFrame worldFrame, AMoveSkill newSkill)
	{
		if ((newSkill != null))
		{
			// switch to new skill
			log.trace("Switch to new skill: " + newSkill);
			
			if (skill != null)
			{
				skill.setWorldFrame(latestWorldFrame);
				enqueueCmds(skill.calcExitActions(bot));
			}
			
			skill = newSkill;
			
			skill.setWorldFrame(latestWorldFrame);
			
			enqueueCmds(skill.calcEntryActions(bot));
			notifySkillStarted(skill);
		} else
		{
			skill.setWorldFrame(worldFrame);
			
			if (skill.isComplete())
			{
				enqueueCmds(skill.calcExitActions(bot));
				notifySkillCompleted(skill);
				skill = null;
				timeLastCompletedSkill = System.nanoTime();
			} else
			{
				// process
				enqueueCmds(skill.calcActions(bot));
			}
		}
	}
	
	
	/**
	 * Clears all skills
	 */
	private synchronized void resetSkills()
	{
		skill = null;
		synchronized (newSkills)
		{
			newSkills.clear();
		}
		timeLastCompletedSkill = System.nanoTime();
	}
	
	
	private void enqueueCmds(List<ACommand> cmds)
	{
		for (ACommand cmd : cmds)
		{
			notifyNewCommand(cmd);
		}
	}
	
	
	private void notifyNewCommand(ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ISkillExecutorObserver observer : observers)
			{
				observer.onNewCommand(cmd);
			}
		}
	}
	
	
	private void notifySkillCompleted(AMoveSkill skill)
	{
		synchronized (observers)
		{
			for (final ISkillExecutorObserver observer : observers)
			{
				observer.onSkillCompleted(skill);
			}
		}
	}
	
	
	private void notifySkillStarted(AMoveSkill skill)
	{
		synchronized (observers)
		{
			for (final ISkillExecutorObserver observer : observers)
			{
				observer.onSkillStarted(skill);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set a new skill
	 * 
	 * @param nSkill
	 * @param provider
	 */
	public void setSkill(AMoveSkill nSkill, ISkillWorldInfoProvider provider)
	{
		// synchronized (this)
		// {
		latestSisyphus = provider.getSisyphus();
		// }
		nSkill.setSisyphus(latestSisyphus);
		nSkill.setPeriod(period);
		synchronized (newSkills)
		{
			if (newSkills.remainingCapacity() == 0)
			{
				try
				{
					newSkills.remove();
				} catch (NoSuchElementException err)
				{
					log.warn("newSkills was modified between statements. No proper synchronization!");
				}
			}
			newSkills.add(nSkill);
		}
	}
	
	
	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		latestWorldFrame = wf;
	}
	
	
	@Override
	public void onVisionSignalLost(WorldFrame emptyWf)
	{
		if (latestWorldFrame != null)
		{
			sentStop(latestWorldFrame);
			latestWorldFrame = null;
		}
	}
	
	
	/**
	 * Sent immediate stop signal
	 * 
	 * @param wFrame
	 */
	public synchronized void sentStop(WorldFrame wFrame)
	{
		if (wFrame == null)
		{
			return;
		}
		
		if (!stopSent)
		{
			final TrackedTigerBot bot = wFrame.tigerBotsVisible.getWithNull(botId);
			if ((bot != null) && (latestSisyphus != null))
			// && ((bot.getBotType() != EBotType.TIGER_V2) || (((TigerBotV2) bot.getBot()).getControllerType() ==
			// ControllerType.FUSION_VEL)))
			{
				AMoveSkill stopSkill = new NormalStopSkill();
				stopSkill.setSisyphus(latestSisyphus);
				process(bot, wFrame, stopSkill);
				stopSent = true;
			}
		}
	}
	
	
	/**
	 * Sent immediate stop signal
	 */
	public void sentStop()
	{
		sentStop(latestWorldFrame);
	}
}
