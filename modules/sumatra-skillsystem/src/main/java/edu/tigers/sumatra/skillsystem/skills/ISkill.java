/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.botmanager.data.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.util.concurrent.ExecutorService;


/**
 * Interface to skills from outside of {@link ASkillSystem}.
 */
public interface ISkill
{
	/**
	 * @param wfw
	 * @param bot
	 * @param shapeMap
	 */
	void update(WorldFrameWrapper wfw, ABot bot, final ShapeMap shapeMap, MatchCommand matchCommand);


	/**
	 * @param timestamp
	 */
	void calcActions(long timestamp);


	/**
	 * Called once upon end of this skill
	 */
	void calcExitActions();


	/**
	 * Called once upon start of this skill
	 */
	void calcEntryActions();


	default void setPrioMap(final PathFinderPrioMap prioMap)
	{
		// ignore it by default
	}

	/**
	 * @return
	 */
	BotID getBotId();


	/**
	 * @return
	 */
	boolean isAssigned();


	/**
	 * @return
	 */
	boolean isInitialized();


	/**
	 * @param control
	 */
	void setMultimediaControl(final MultimediaControl control);


	/**
	 * Create bot ai infos based on current state
	 *
	 * @return new bot ai info structure
	 */
	BotAiInformation getBotAiInfo();

	/**
	 * Set the executor service for path planning.
	 *
	 * @param executorService
	 */
	default void setExecutorService(ExecutorService executorService)
	{
	}

	/**
	 * @return the last applied trajectory
	 */
	default TrajectoryWithTime<IVector3> getCurrentTrajectory()
	{
		return null;
	}

	default void setCurrentTrajectory(TrajectoryWithTime<IVector3> trajectory)
	{
	}
}
