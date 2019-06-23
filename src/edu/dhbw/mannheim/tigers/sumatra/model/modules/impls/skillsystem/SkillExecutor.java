/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): AndreR
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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ImmediateStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;


/**
 * Skill executer with nano second precision.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SkillExecutor implements Runnable, IWorldFrameConsumer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger						log							= Logger.getLogger(SkillExecutor.class
																										.getName());
	
	private static final long							NO_SKILL_TIMEOUT_NS		= TimeUnit.MILLISECONDS.toNanos(500);
	
	private ISkill											skill							= null;
	
	private final ABot									bot;
	
	/** [ns] */
	private final long									period;
	
	/** Volatile because it is written from the outside */
	private volatile WorldFrame						latestWorldFrame			= null;
	private final List<ISkillExecutorObserver>	observers					= new ArrayList<ISkillExecutorObserver>();
	
	private final BlockingDeque<ISkill>				newSkills					= new LinkedBlockingDeque<ISkill>(1);
	
	private long											timeLastCompletedSkill	= 0;
	private boolean										stopSent						= false;
	
	private final ISkillWorldInfoProvider			provider;
	
	private final Object									syncProcess					= new Object();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param bot
	 * @param period [us]
	 * @param provider
	 */
	public SkillExecutor(final ABot bot, final long period, final ISkillWorldInfoProvider provider)
	{
		this.bot = bot;
		this.period = TimeUnit.MICROSECONDS.toNanos(period);
		this.provider = provider;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillExecutorObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillExecutorObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	@Override
	public void run()
	{
		try
		{
			WorldFrame worldFrame = latestWorldFrame;
			if (bot.getNetworkState() != ENetworkState.ONLINE)
			{
				return;
			}
			if ((worldFrame != null) && (worldFrame.getBot(bot.getBotID()) == null))
			{
				return;
			}
			ISkill newSkill;
			synchronized (newSkills)
			{
				newSkill = newSkills.poll();
			}
			TrackedTigerBot tBot = null;
			boolean insideField = true;
			if (worldFrame != null)
			{
				tBot = worldFrame.getBot(bot.getBotID());
				insideField = AIConfig.getGeometry().getFieldWReferee().isPointInShape(tBot.getPos());
			}
			if (((skill != null) || (newSkill != null)) && insideField)
			{
				
				if (((tBot == null) || (worldFrame == null)) && ((newSkill != null) && (newSkill.needsVision())))
				{
					resetSkills();
					return;
				}
				if (worldFrame == null)
				{
					worldFrame = new WorldFrame(WorldFrameFactory.createEmptyWorldFrame(0), bot.getColor(), false);
				}
				process(tBot, worldFrame, newSkill);
				if ((skill != null) && (skill.getSkillName() != ESkillName.IMMEDIATE_STOP))
				{
					stopSent = false;
				}
			} else if (!stopSent && ((System.nanoTime() - timeLastCompletedSkill) > NO_SKILL_TIMEOUT_NS))
			{
				sentStop();
			}
		} catch (Throwable err)
		{
			log.error("Exception in Skillexecuter: " + err.getMessage(), err);
			resetSkills();
		}
	}
	
	
	private void process(final TrackedTigerBot tBot, final WorldFrame worldFrame, final ISkill newSkill)
	{
		synchronized (syncProcess)
		{
			if ((newSkill != null))
			{
				// switch to new skill
				log.trace("Switch to new skill: " + newSkill);
				
				if (skill != null)
				{
					skill.setWorldFrame(worldFrame);
					enqueueCmds(skill.calcExitActions());
				}
				
				skill = newSkill;
				
				skill.setWorldFrame(worldFrame);
				if (tBot != null)
				{
					skill.settBot(tBot);
				}
				skill.setBot(bot);
				
				enqueueCmds(skill.calcEntryActions());
				notifySkillStarted(skill);
			} else
			{
				skill.setWorldFrame(worldFrame);
				if (tBot != null)
				{
					skill.settBot(tBot);
				}
				
				if (skill.isComplete())
				{
					enqueueCmds(skill.calcExitActions());
					notifySkillCompleted(skill);
					skill = null;
					timeLastCompletedSkill = System.nanoTime();
				} else
				{
					// process
					enqueueCmds(skill.calcActions());
				}
			}
		}
	}
	
	
	/**
	 * Clears all skills
	 */
	private void resetSkills()
	{
		if (skill != null)
		{
			enqueueCmds(skill.calcExitActions());
			notifySkillCompleted(skill);
			skill = null;
		}
		synchronized (newSkills)
		{
			newSkills.clear();
		}
		timeLastCompletedSkill = System.nanoTime();
	}
	
	
	private void enqueueCmds(final List<ACommand> cmds)
	{
		for (ACommand cmd : cmds)
		{
			notifyNewCommand(cmd);
		}
	}
	
	
	private void notifyNewCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ISkillExecutorObserver observer : observers)
			{
				observer.onNewCommand(cmd);
			}
		}
	}
	
	
	private void notifySkillCompleted(final ISkill skill)
	{
		synchronized (observers)
		{
			for (final ISkillExecutorObserver observer : observers)
			{
				observer.onSkillCompleted(skill);
			}
		}
	}
	
	
	private void notifySkillStarted(final ISkill skill)
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
	 */
	public void setSkill(final ISkill nSkill)
	{
		nSkill.setSisyphus(provider.getSisyphus());
		nSkill.setDt(period);
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
	public void onNewSimpleWorldFrame(final SimpleWorldFrame wf)
	{
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrame wFrame)
	{
		if ((bot != null) && (bot.getColor() == wFrame.getTeamColor()))
		{
			latestWorldFrame = wFrame;
		}
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		sentStop();
		latestWorldFrame = null;
	}
	
	
	/**
	 * Sent immediate stop signal
	 */
	private void sentStop()
	{
		if (!stopSent)
		{
			stopSent = true;
			AMoveSkill stopSkill = new ImmediateStopSkill();
			stopSkill.setSisyphus(provider.getSisyphus());
			WorldFrame wFrame = new WorldFrame(WorldFrameFactory.createEmptyWorldFrame(0), bot.getColor(), false);
			process(null, wFrame, stopSkill);
		}
	}
	
	
	@Override
	public void onStop()
	{
	}
}
