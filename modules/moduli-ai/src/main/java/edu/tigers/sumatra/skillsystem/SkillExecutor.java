/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.IMatchCommand;
import edu.tigers.sumatra.botmanager.commands.MatchCommand;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Execute skills for a single bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SkillExecutor implements Runnable, IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger							log					= Logger.getLogger(SkillExecutor.class.getName());
																							
	private ISkill												currentSkill		= new IdleSkill();
	private ISkill												newSkill				= new IdleSkill();
																							
	private final ABot										bot;
	private final Object										newSkillSync		= new Object();
																							
																							
	private final BlockingDeque<WorldFrameWrapper>	freshWorldFrames	= new LinkedBlockingDeque<WorldFrameWrapper>(
																									1);
	private boolean											active				= true;
	private List<ISkillExecuterPostHook>				postHooks			= new ArrayList<>(1);
	private Future<?>											future				= null;
																							
																							
	/**
	 * @param bot
	 */
	public SkillExecutor(final ABot bot)
	{
		this.bot = bot;
		currentSkill.setBotId(bot.getBotId());
	}
	
	
	/**
	 * @param wf
	 */
	public void update(final WorldFrameWrapper wf)
	{
		assert wf != null;
		
		MatchCommand matchCtrl = bot.getMatchCtrl();
		final ISkill nextSkill;
		if (!wf.getSimpleWorldFrame().getBots().containsKey(bot.getBotId()) &&
				(currentSkill.getType() != ESkill.IDLE))
		{
			nextSkill = new IdleSkill();
		} else
		{
			synchronized (newSkillSync)
			{
				nextSkill = newSkill;
				newSkill = null;
			}
		}
		processNewSkill(nextSkill, wf, matchCtrl);
		executeSave(() -> currentSkill.update(wf, bot));
		executeSave(() -> currentSkill.calcActions());
	}
	
	
	/**
	 * @param skill
	 */
	public void setNewSkill(final ISkill skill)
	{
		synchronized (newSkillSync)
		{
			newSkill = skill;
		}
	}
	
	
	private void processNewSkill(final ISkill skill, final WorldFrameWrapper wf, final IMatchCommand matchCtrl)
	{
		if (skill == null)
		{
			return;
		}
		
		if (currentSkill.isInitialized())
		{
			executeSave(() -> currentSkill.calcExitActions());
		}
		executeSave(() -> skill.update(wf, bot));
		executeSave(() -> skill.calcEntryActions());
		currentSkill = skill;
	}
	
	
	private void executeSave(final Runnable run)
	{
		try
		{
			run.run();
		} catch (Throwable err)
		{
			log.error("Exception in SkillExecutor " + bot.getBotId(), err);
		}
	}
	
	
	/**
	 * @return the currentSkill
	 */
	public ISkill getCurrentSkill()
	{
		return currentSkill;
	}
	
	
	/**
	 * @return the bot
	 */
	public IBot getBot()
	{
		return bot;
	}
	
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("SkillExecutor " + bot.getBotId());
		WorldFrameWrapper lastWf = null;
		while (active)
		{
			try
			{
				long timeout = Long.MAX_VALUE;
				if (bot.getMinUpdateRate() > 0)
				{
					timeout = (long) (1000.0 / bot.getMinUpdateRate());
				}
				WorldFrameWrapper wf = freshWorldFrames.poll(timeout, TimeUnit.MILLISECONDS);
				// long t0 = System.nanoTime();
				if (wf == null)
				{
					if (lastWf != null)
					{
						update(lastWf);
					}
					bot.sendMatchCommand();
					postHooks.forEach(h -> h.onCommandSent(bot, System.nanoTime()));
				} else
				{
					update(wf);
					postHooks.forEach(h -> h.onCommandSent(bot, wf.getSimpleWorldFrame().getTimestamp()));
					lastWf = wf;
				}
				// long t1 = System.nanoTime();
				// double dt = (t1 - t0) / 1e9;
				// if (dt > 0.01)
				// {
				// System.out.println(getCurrentSkill().getType().name() + " " + dt);
				// }
			} catch (InterruptedException err)
			{
				break;
			}
		}
		Thread.currentThread().setName("SkillExecutor not assigned");
	}
	
	
	/**
	 * @param service
	 */
	public void start(final ExecutorService service)
	{
		future = service.submit(this);
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		active = false;
		future.cancel(true);
		future = null;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		freshWorldFrames.clear();
		freshWorldFrames.addLast(wFrameWrapper);
	}
	
	
	@Override
	public void onClearWorldFrame()
	{
		setNewSkill(new IdleSkill());
	}
	
	
	/**
	 * @param hook
	 */
	public void addPostHook(final ISkillExecuterPostHook hook)
	{
		postHooks.add(hook);
	}
	
	
	/**
	 * @param hook
	 */
	public void removePostHook(final ISkillExecuterPostHook hook)
	{
		postHooks.remove(hook);
	}
}
