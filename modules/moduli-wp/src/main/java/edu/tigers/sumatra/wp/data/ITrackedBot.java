/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrackedBot extends ITrackedObject
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
	 * @return the visible
	 */
	boolean isVisible();
	
	
	/**
	 * @return
	 */
	double getCenter2DribblerDist();
	
	
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
	double getaAcc();
	
	
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
	
}