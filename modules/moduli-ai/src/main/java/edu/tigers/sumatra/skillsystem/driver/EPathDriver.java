/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EPathDriver
{
	/**  */
	POSITION,
	/**  */
	MIXED,
	/**  */
	MIXED_SPLINE_POS,
	/**  */
	MIXED_LONG_POS,
	/**  */
	PATH_POINT,
	/**  */
	HERMITE_SPLINE,
	/**  */
	BSPLINE,
	/**  */
	PULL_BALL_PATH,
	/**  */
	LONG,
	
	/**  */
	AROUND_BALL,
	/**  */
	DO_NOTHING,
	/**  */
	PUSH_BALL,
	/**  */
	LATENCY,
	/**  */
	CUSTOM,
	/**  */
	PULL_BALL,
	/**  */
	TOWARDS_BALL,
	/**  */
	KICK_TRAJ,
	/**  */
	KICK_CHILL_TRAJ,
	/**  */
	CIRCLE,
	/**  */
	FUZZY,
	/**  */
	MOVE_TO,
	/**  */
	TRAJ_PATH,
	/**  */
	KICK_BALL_V2,
	/**  */
	KICK_BALL,
	/**  */
	CATCH_BALL,
	/**  */
	TURN_WITH_BALL,
	/**  */
	KICK_BALL_SPLINE,
	/** */
	VELOCITY;
}
