/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.vector.IVector2;


public interface IMovingRobot
{
	/**
	 * Get the horizon for possible movement of the robot for a given time horizon
	 *
	 * @param tHorizon the time horizon
	 * @return a circle specifying the horizon
	 */
	default ICircle getMovingHorizon(final double tHorizon)
	{
		return getMovingHorizon(tHorizon, 0);
	}

	/**
	 * Get the horizon for possible movement of the robot for a given time horizon
	 *
	 * @param tHorizon            the time horizon
	 * @param tAdditionalReaction additional reaction time
	 * @return a circle specifying the horizon
	 */
	ICircle getMovingHorizon(double tHorizon, double tAdditionalReaction);


	/**
	 * Get a tube with fixed width and length depending on the moving horizon.
	 *
	 * @param tHorizon the time horizon
	 * @return a tube specifying the movement horizon
	 */
	default ITube getMovingHorizonTube(final double tHorizon)
	{
		return getMovingHorizonTube(tHorizon, 0);
	}

	/**
	 * Get a tube with fixed width and length depending on the moving horizon.
	 *
	 * @param tHorizon            the time horizon
	 * @param tAdditionalReaction additional reaction time
	 * @return a tube specifying the movement horizon
	 */
	ITube getMovingHorizonTube(double tHorizon, double tAdditionalReaction);

	/**
	 * @return the position of the robot (at t=0)
	 */
	IVector2 getPos();

	/**
	 * @return the current speed of the robot (at t=0)
	 */
	double getSpeed();
}
