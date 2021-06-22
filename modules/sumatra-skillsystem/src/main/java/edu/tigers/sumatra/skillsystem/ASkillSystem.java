/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The base class for every implementation of a skill system
 */
public abstract class ASkillSystem extends AModule
{
	@Getter
	private ExecutorService executorService;
	private final List<ISkillSystemObserver> observers = new CopyOnWriteArrayList<>();


	/**
	 * @param botId
	 * @param skill
	 */
	public abstract void execute(BotID botId, ISkill skill);


	/**
	 * @param botId
	 */
	public abstract void reset(final BotID botId);


	/**
	 * @param color
	 */
	public abstract void reset(ETeamColor color);


	/**
	 * @param teamColor
	 * @return
	 */
	public abstract List<ISkill> getCurrentSkills(final ETeamColor teamColor);


	public abstract Map<BotID, ShapeMap> process(final WorldFrameWrapper wfw, final ETeamColor teamColor);


	@Override
	public void deinitModule()
	{
	}


	@Override
	public void initModule()
	{
	}


	@Override
	public void startModule()
	{
		executorService = Executors.newCachedThreadPool(new NamedThreadFactory("SkillExecutor"));
	}


	@Override
	public void stopModule()
	{
		executorService.shutdown();
	}


	/**
	 * Stop all bots as fast as possible
	 */
	public abstract void emergencyStop();


	/**
	 * Stop all bots of certain color as fast as possible
	 *
	 * @param teamColor the color of the team to send the emergency to
	 */
	public abstract void emergencyStop(ETeamColor teamColor);


	protected void notifyCommandSent(final ABot bot, final long timestamp)
	{
		for (ISkillSystemObserver observer : observers)
		{
			observer.onCommandSent(bot, timestamp);
		}
	}


	public void addObserver(final ISkillSystemObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(final ISkillSystemObserver observer)
	{
		observers.remove(observer);
	}


	public abstract void addSkillExecutorPostHook(ISkillExecutorPostHook hook);


	public abstract void removeSkillExecutorPostHook(ISkillExecutorPostHook hook);
}
