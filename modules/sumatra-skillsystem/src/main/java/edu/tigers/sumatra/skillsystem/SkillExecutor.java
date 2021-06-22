/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.util.BotStateTrajectorySync;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;


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
	private final BotStateTrajectorySync botStateTrajectorySync = new BotStateTrajectorySync();
	private ABot bot;
	private CountDownLatch wfProcessedLatch = new CountDownLatch(1);
	private ISkill currentSkill = new IdleSkill();
	private ISkill newSkill = new IdleSkill();
	private boolean active = true;
	private List<ISkillExecutorPostHook> postHooks = new CopyOnWriteArrayList<>();
	private Future<?> future = null;
	private boolean notifiedBotRemoved = true;
	private ExecutorService executorService;


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
		validateCurrentTrajectory(wfw, shapeMap, currentBot);
		shapeMap.setInverted(wfw.getWorldFrame(EAiTeam.primary(botID.getTeamColor())).isInverted());
		postHooks.forEach(h -> h.onSkillUpdated(currentBot, wfw.getTimestamp(), shapeMap));
	}


	private void processCurrentSkill(WorldFrameWrapper wf, ShapeMap shapeMap, ABot currentBot)
	{
		executeSave(() -> currentSkill.update(wf, currentBot, shapeMap));
		executeSave(() -> currentSkill.calcActions(wf.getTimestamp()));
		executeSave(currentBot::sendMatchCommand);
	}


	private void validateCurrentTrajectory(WorldFrameWrapper wfw, ShapeMap shapeMap, ABot currentBot)
	{
		var wFrame = wfw.getWorldFrame(EAiTeam.primary(botID.getTeamColor()));
		var tBot = wFrame.getBot(currentBot.getBotId());
		if (tBot == null)
		{
			return;
		}
		if (botCollidingWithOtherBot(wFrame) || currentSkill.getCurrentTrajectory() == null)
		{
			botStateTrajectorySync.reset();
		} else
		{
			var synchronizedTrajectory = currentSkill.getCurrentTrajectory().synchronizeTo(wfw.getTimestamp());
			botStateTrajectorySync.add(synchronizedTrajectory, wfw.getTimestamp());
		}
		var currentBotState = tBot.getCurrentState();
		botStateTrajectorySync.updateState(wfw.getTimestamp(), currentBotState);
		if (!botStateTrajectorySync.isOnTrack())
		{
			currentSkill.setCurrentTrajectory(null);
		}
		shapeMap.get(ESkillShapesLayer.BUFFERED_TRAJECTORY).add(createDistanceToTrajectoryShape(tBot));
		botStateTrajectorySync.getLatestState().ifPresent(state ->
				shapeMap.get(ESkillShapesLayer.BUFFERED_TRAJECTORY).add(createBotBufferedTrajShape(tBot, state)));
		postHooks.forEach(h -> h.onTrajectoryUpdated(botID, botStateTrajectorySync));
		currentBot.setCurrentTrajectory(currentSkill.getCurrentTrajectory());
	}


	private boolean botCollidingWithOtherBot(WorldFrame wFrame)
	{
		var otherBotStates = wFrame.getBots().values().stream()
				.filter(b -> b.getBotId() != botID)
				.map(ITrackedBot::getCurrentState)
				.collect(Collectors.toList());
		var tBot = wFrame.getBot(getBotID());
		double margin = Geometry.getBotRadius() * 2 + 10;
		return otherBotStates.stream().anyMatch(s -> s.getPos().distanceTo(tBot.getPos()) < margin);
	}


	private DrawableAnnotation createDistanceToTrajectoryShape(final ITrackedBot bot)
	{
		var quality = botStateTrajectorySync.getTrajTrackingQuality();
		String text = String.format("%.0f>%.0f: %.2fs",
				quality.getCurDistance(),
				quality.getMaxDistance(),
				quality.getTimeOffTrajectory());
		var color = quality.getCurDistance() > quality.getMaxDistance()
				? Color.red
				: quality.getTimeOffTrajectory() > 0
				? Color.orange
				: Color.green;
		return new DrawableAnnotation(bot.getPos(), text)
				.withOffset(Vector2.fromY(-200))
				.withCenterHorizontally(true)
				.setColor(color);
	}


	private DrawableBotShape createBotBufferedTrajShape(final ITrackedBot bot, State state)
	{
		Pose pose = state.getPose();
		DrawableBotShape botShape = new DrawableBotShape(pose.getPos(), pose.getOrientation(),
				Geometry.getBotRadius(), bot.getRobotInfo().getCenter2DribblerDist());
		botShape.setFillColor(null);
		botShape.setBorderColor(Color.LIGHT_GRAY);
		botShape.setFontColor(Color.LIGHT_GRAY);
		botShape.setId(String.valueOf(bot.getBotId().getNumber()));
		return botShape;
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
		skill.setExecutorService(executorService);
		executeSave(() -> skill.update(wf, currentBot, shapeMap));
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
		Thread.currentThread().setName("SkillExecutor not assigned");
	}


	/**
	 * @param service
	 */
	public void start(final ExecutorService service)
	{
		this.executorService = service;
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
