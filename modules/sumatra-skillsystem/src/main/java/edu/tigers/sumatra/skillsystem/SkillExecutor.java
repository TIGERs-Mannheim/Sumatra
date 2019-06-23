/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Execute skills for a single bot
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
class SkillExecutor implements Runnable, IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(SkillExecutor.class.getName());
	private final ABot bot;
	private final Object newSkillSync = new Object();
	private final BlockingDeque<WorldFrameWrapper> freshWorldFrames = new LinkedBlockingDeque<>(
			1);
	private ISkill currentSkill;
	private ISkill newSkill = new IdleSkill();
	private boolean active = true;
	private List<ISkillExecuterPostHook> postHooks = new CopyOnWriteArrayList<>();
	private Future<?> future = null;
	private boolean processAllWorldFrames = false;
	private ATimer timer = null;
	private final String timeable;
	
	private static final double BOT_MIN_UPDATE_RATE = 10;
	
	
	/**
	 * @param bot
	 */
	SkillExecutor(final ABot bot)
	{
		this.bot = bot;
		timeable = "Skill " + bot.getBotId();
		currentSkill = new IdleSkill();
		currentSkill.update(null, bot);
	}
	
	
	/**
	 * @param wf
	 * @param timestamp
	 */
	public void update(final WorldFrameWrapper wf, final long timestamp)
	{
		final ISkill nextSkill;
		if ((wf != null) && !wf.getSimpleWorldFrame().getBots().containsKey(bot.getBotId()) &&
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
		processNewSkill(nextSkill, wf);
		executeSave(() -> currentSkill.update(wf, bot));
		executeSave(() -> currentSkill.calcActions(timestamp));
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
	
	
	private void processNewSkill(final ISkill skill, final WorldFrameWrapper wf)
	{
		if (skill == null)
		{
			return;
		}
		
		if (currentSkill.isInitialized())
		{
			executeSave(currentSkill::calcExitActions);
		}
		executeSave(() -> skill.update(wf, bot));
		executeSave(skill::calcEntryActions);
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
	ISkill getCurrentSkill()
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
				if (bot.getType() == EBotType.TIGER_V3)
				{
					timeout = (long) (1000.0 / BOT_MIN_UPDATE_RATE);
				}
				WorldFrameWrapper wf = freshWorldFrames.poll(timeout, TimeUnit.MILLISECONDS);
				long timestamp;
				if (wf == null)
				{
					timestamp = System.nanoTime();
				} else
				{
					startTimer(wf.getSimpleWorldFrame().getId());
					timestamp = wf.getSimpleWorldFrame().getTimestamp();
					lastWf = wf;
				}
				update(lastWf, timestamp);
				postHooks.forEach(h -> h.onCommandSent(bot, timestamp));
				if (wf != null)
				{
					stopTimer(wf.getSimpleWorldFrame().getId());
				}
			} catch (InterruptedException err)
			{
				// ignore
				Thread.currentThread().interrupt();
			} catch (Throwable err)
			{
				log.fatal("Some fatal error occurred in skill executor.", err);
			}
		}
		Thread.currentThread().setName("SkillExecutor not assigned");
	}
	
	
	private void startTimer(final long id)
	{
		if (timer != null)
		{
			timer.start(timeable, id);
		}
	}
	
	
	private void stopTimer(final long id)
	{
		if (timer != null)
		{
			timer.stop(timeable, id);
		}
	}
	
	
	/**
	 * @param service
	 */
	public void start(final ExecutorService service)
	{
		try
		{
			timer = (ATimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (ModuleNotFoundException e)
		{
			log.info("No timer module found.", e);
		}
		future = service.submit(this);
	}
	
	
	/**
	 * Stop this executor
	 */
	public void stop()
	{
		active = false;
		future.cancel(true);
		future = null;
		timer = null;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (active && processAllWorldFrames)
		{
			try
			{
				freshWorldFrames.putFirst(wFrameWrapper);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		} else
		{
			freshWorldFrames.pollLast();
			freshWorldFrames.addFirst(wFrameWrapper);
		}
	}
	
	
	/**
	 * @param processAllWorldFrames
	 */
	public void setProcessAllWorldFrames(final boolean processAllWorldFrames)
	{
		this.processAllWorldFrames = processAllWorldFrames;
	}
	
	
	@Override
	public void onClearWorldFrame()
	{
		setNewSkill(new IdleSkill());
	}
	
	
	/**
	 * @param hook
	 */
	void addPostHook(final ISkillExecuterPostHook hook)
	{
		postHooks.add(hook);
	}
	
	
	/**
	 * @param hook
	 */
	void removePostHook(final ISkillExecuterPostHook hook)
	{
		postHooks.remove(hook);
	}
}
