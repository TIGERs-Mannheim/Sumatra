/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;

import java.util.Optional;


/**
 * Interface for a (real) robot.
 */
public interface IBot
{
	/**
	 * @return
	 */
	EBotType getType();

	/**
	 * @return the botId
	 */
	BotID getBotId();

	/**
	 * @return
	 */
	IBotParams getBotParams();

	/**
	 * @return the current bot trajectory in the coordinate system of the AI (you may have to mirror it when accessing
	 * outside AI or for opponent bot)
	 */
	default Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory()
	{
		return Optional.empty();
	}

	/**
	 * @return the controlledBy
	 */
	String getControlledBy();

	/**
	 * @return
	 */
	boolean isAvailableToAi();


	/**
	 * @return the color
	 */
	ETeamColor getColor();


	/**
	 * @return the manualControl
	 */
	boolean isBlocked();

	/**
	 * Simplified health state, mainly used for automatic interchange.
	 * @return
	 */
	ERobotHealthState getHealthState();

	/**
	 * Get version string.
	 *
	 * @return
	 */
	default String getVersionString()
	{
		return "No versioning";
	}
}
