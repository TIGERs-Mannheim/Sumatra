/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Constraints on the movement of robots. A read-only view.
 */
public interface IMoveConstraints
{
	double getVelMax();


	double getVelMaxW();


	double getAccMax();


	double getAccMaxFast();


	default double getAccMaxDerived()
	{
		return isFastMove() ? getAccMaxFast() : getAccMax();
	}


	double getBrkMax();


	double getAccMaxW();


	double getJerkMax();


	double getJerkMaxW();


	boolean isFastMove();


	IVector2 getPrimaryDirection();
}
