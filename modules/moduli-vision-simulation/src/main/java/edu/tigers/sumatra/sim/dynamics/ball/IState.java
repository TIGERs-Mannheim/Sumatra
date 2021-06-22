/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.dynamics.ball;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 */
public interface IState
{

	/**
	 * @return the pos [mm]
	 */
	IVector3 getPos();


	/**
	 * @return the vel [mm/s]
	 */
	IVector3 getVel();


	/**
	 * @return the acc [mm/s^2]
	 */
	IVector3 getAcc();

	/**
	 * @return spin in [rad/s]
	 */
	IVector2 getSpin();
}