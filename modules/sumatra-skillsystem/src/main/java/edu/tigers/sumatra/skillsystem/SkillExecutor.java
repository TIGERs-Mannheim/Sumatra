/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * Execute skills for a single bot
 */
@Log4j2
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class SkillExecutor implements Runnable, IWorldFrameObserver
{
	private final BotID botID;
	private final NewSkillSync newSkillSync = new NewSkillSync();
	private final BlockingDeque<WorldFrameWrapper> freshWorldFrames = new LinkedBlockingDeque<>(1);
	private ABot bot;
	private CountDownLatch wfProcessedLatch = new CountDownLatch(1);
	private ISkill currentSkill = new IdleSkill();
	private ISkill newSkill = new IdleSkill();
	private boolean active = true;
	private List<ISkillExecutorPostHook> postHooks = new CopyOnWriteArrayList<>();
	private Future<?> future = null;
	private boolean notifiedBotRemoved = true;
	private final MatchCommand matchCommand = new MatchCommand();


	public void update(final WorldFrameWrapper wf, final ShapeMap shapeMap)
	{
		update(wf, shapeMap, bot);
	}


	private void update(final WorldFrameWrapper wfw, final ShapeMap shapeMap, final ABot currentBot)
	{
		if (currentBot == null)
		{
			notifyBotRemoved();
			return;
		}
		notifiedBotRemoved = false;
		final ISkill nextSkill;
		if (!wfw.getSimpleWorldFrame().getBots().containsKey(botID) &&
				(currentSkill.getClass() != IdleSkill.class))
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
		processNewSkill(nextSkill, wfw, currentBot, shapeMap);
		processCurrentSkill(wfw, shapeMap, currentBot);
		currentBot.setCurrentTrajectory(currentSkill.getCurrentTrajectory());
		shapeMap.setInverted(wfw.getWorldFrame(EAiTeam.primary(botID.getTeamColor())).isInverted());
		postHooks.forEach(h -> h.onSkillUpdated(currentBot, wfw.getTimestamp(), shapeMap));
	}


	private void processCurrentSkill(WorldFrameWrapper wf, ShapeMap shapeMap, ABot currentBot)
	{
		executeSave(() -> currentSkill.update(wf, currentBot, shapeMap, matchCommand));
		executeSave(() -> currentSkill.calcActions(wf.getTimestamp()));
		executeSave(() -> currentBot.sendMatchCommand(matchCommand));
	}


	private void notifyBotRemoved()
	{
		if (!notifiedBotRemoved)
		{
			postHooks.forEach(o -> o.onRobotRemoved(botID));
			notifiedBotRemoved = true;
		}
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


	public void setNewBot(final ABot bot)
	{
		this.bot = bot;
	}


	private void processNewSkill(final ISkill skill, final WorldFrameWrapper wf, final ABot currentBot,
			final ShapeMap shapeMap)
	{
		if (skill == null)
		{
			return;
		}

		if (currentSkill.isInitialized())
		{
			executeSave(currentSkill::calcExitActions);
		}
		skill.setCurrentTrajectory(currentSkill.getCurrentTrajectory());
		executeSave(() -> skill.update(wf, currentBot, shapeMap, matchCommand));
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
			log.error("Exception in SkillExecutor " + botID, err);
		}
	}


	/**
	 * @return the currentSkill
	 */
	ISkill getCurrentSkill()
	{
		return currentSkill;
	}


	public BotID getBotID()
	{
		return botID;
	}


	@Override
	public void run()
	{
		Thread.currentThread().setName("SkillExecutor " + botID);
		while (active)
		{
			try
			{
				WorldFrameWrapper wf = freshWorldFrames.take();
				ThreadContext.put("wfTs", String.valueOf(wf.getSimpleWorldFrame().getTimestamp()));
				ThreadContext.put("wfId", String.valueOf(wf.getSimpleWorldFrame().getFrameNumber()));
				update(wf, new ShapeMap(), bot);
			} catch (InterruptedException err)
			{
				// ignore
				Thread.currentThread().interrupt();
			} catch (Throwable err)
			{
				log.fatal("Some fatal error occurred in skill executor.", err);
			}
			wfProcessedLatch.countDown();
		}
		postHooks.clear();
		ThreadContext.remove("wfTs");
		ThreadContext.remove("wfId");
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
	 * Stop this executor
	 */
	public void stop()
	{
		active = false;
		if (future != null)
		{
			future.cancel(true);
			future = null;
		}
		wfProcessedLatch.countDown();
	}


	public void waitUntilWorldFrameProcessed()
	{
		try
		{
			wfProcessedLatch.await();
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (SumatraModel.getInstance().isSimulation())
		{
			wfProcessedLatch = new CountDownLatch(1);
		}
		freshWorldFrames.pollLast();
		freshWorldFrames.addFirst(wFrameWrapper);
	}


	@Override
	public void onClearWorldFrame()
	{
		setNewSkill(new IdleSkill());
	}


	void addPostHook(final ISkillExecutorPostHook hook)
	{
		postHooks.add(hook);
	}


	@SuppressWarnings("squid:S2094") // empty class for named monitor class
	private static class NewSkillSync
	{
	}
}
