/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
	 * @return
	 */
	double getDribblerSpeed();
	
	
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
	
	
	/**
	 * @return the manualControl
	 */
	boolean isBlocked();
	
	
	/**
	 * @return the excludeFromAi
	 */
	boolean isHideFromAi();
	
	
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
	 * @return
	 */
	String getName();
	
	
	/**
	 * Get internal position from sensory data
	 *
	 * @return
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	Optional<IVector3> getSensoryPos();
	
	
	/**
	 * Get internal velcoity from sensory data
	 *
	 * @return
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	Optional<IVector3> getSensoryVel();
	
	
	/**
	 * @return
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
}