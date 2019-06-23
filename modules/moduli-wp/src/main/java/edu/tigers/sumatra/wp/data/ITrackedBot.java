/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import java.util.Optional;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrackedBot extends ITrackedObject, IExportable
{
	@Override
	ITrackedBot mirrored();
	
	
	/**
	 * @param t
	 * @return
	 */
	IVector2 getPosByTime(double t);
	
	
	/**
	 * @param t
	 * @return
	 */
	IVector2 getVelByTime(double t);
	
	
	/**
	 * @param t
	 * @return
	 */
	double getAngleByTime(double t);
	
	
	/**
	 * @return the ballContact
	 */
	boolean hasBallContact();
	
	
	/**
	 * @param horizon the time horizon in seconds
	 * @return true, if the ball had ball contact within given horizon
	 */
	boolean hadBallContact(double horizon);
	
	
	/**
	 * @return the last time when ball contact was reported
	 */
	long getLastBallContact();
	
	
	/**
	 * @return
	 */
	double getCenter2DribblerDist();
	
	
	/**
	 * @return the bot shape of this bot
	 */
	IBotShape getBotShape();
	
	
	/**
	 * Calculates the position of the dribbler/kicker of the given bot.
	 * Use this position for ball receivers, etc.
	 * 
	 * @return
	 */
	IVector2 getBotKickerPos();
	
	
	/**
	 * @return id of the bot
	 */
	BotID getBotId();
	
	
	/**
	 * @return
	 */
	ETeamColor getTeamColor();
	
	
	/**
	 * @return the angle
	 */
	double getOrientation();
	
	
	/**
	 * @return the aVel
	 */
	double getAngularVel();
	
	
	/**
	 * @return
	 */
	RobotInfo getRobotInfo();
	
	
	/**
	 * @param t
	 * @return
	 */
	IVector2 getBotKickerPosByTime(double t);
	
	
	/**
	 * @return
	 */
	MoveConstraints getMoveConstraints();
	
	
	/**
	 * @return the current robot state
	 */
	State getBotState();
	
	
	/**
	 * @return the current robot state as reported by the vision filter
	 */
	Optional<State> getFilteredState();
	
	
	/**
	 * @return the buffered state of the current trajectory, synchronized with the filtered state
	 */
	Optional<State> getBufferedTrajState();
}