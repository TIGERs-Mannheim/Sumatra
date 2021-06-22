/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBot
{

	/**
	 * @return battery level between 0 and 1
	 */
	double getBatteryRelative();


	/**
	 * @return battery level
	 */
	default double getBatteryAbsolute()
	{
		return 0;
	}


	/**
	 * @return
	 */
	double getKickerLevel();


	/**
	 * The absolute maximum kicker level possible for the bot (not the currently set max cap!)
	 *
	 * @return [V]
	 */
	double getKickerLevelMax();


	/**
	 * Each bot has its own hardware id that uniquely identifies a robot by hardware (mainboard)
	 *
	 * @return
	 */
	int getHardwareId();


	/**
	 * @return
	 */
	boolean isAvailableToAi();


	/**
	 * @return
	 */
	EBotType getType();


	/**
	 * @return the botFeatures
	 */
	Map<EFeature, EFeatureState> getBotFeatures();


	/**
	 * @return the controlledBy
	 */
	String getControlledBy();


	/**
	 * @return the color
	 */
	ETeamColor getColor();


	EDribblerState getDribblerState();


	/**
	 * @return the manualControl
	 */
	boolean isBlocked();


	/**
	 * @return the hideFromRcm
	 */
	boolean isHideFromRcm();


	/**
	 * @return the botId
	 */
	BotID getBotId();


	/**
	 * @return
	 */
	default double getCenter2DribblerDist()
	{
		return getBotParams().getDimensions().getCenter2DribblerDist();
	}


	/**
	 * Get internal state from sensory data
	 *
	 * @return
	 */
	Optional<BotState> getSensoryState();


	/**
	 * @return the current bot trajectory in the coordinate system of the AI (you may have to mirror it when accessing
	 *         outside AI or for opponent bot)
	 */
	default Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory()
	{
		return Optional.empty();
	}


	/**
	 * @return
	 */
	IBotParams getBotParams();


	/**
	 * @return
	 */
	boolean isBarrierInterrupted();


	/**
	 * @return
	 */
	ERobotMode getRobotMode();


	/**
	 * Is true iff the bot is completely okay. (Used for automatic interchange)
	 *
	 * @return
	 */
	boolean isOK();


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
