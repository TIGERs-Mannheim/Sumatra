/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import edu.tigers.sumatra.math.vector.IVector3;


public interface IBallPlacer
{
	/**
	 * Place the ball to a new position.
	 *
	 * @param pos
	 * @param vel
	 */
	void placeBall(final IVector3 pos, final IVector3 vel);
}
