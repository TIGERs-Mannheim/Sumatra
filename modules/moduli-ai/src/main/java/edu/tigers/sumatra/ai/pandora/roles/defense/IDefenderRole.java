/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Common methods for all defender roles
 */
public interface IDefenderRole
{
	/**
	 * if icing was detected, calc new position outside forbidden zone around ball travelline
	 * 
	 * @param pos new target position of bot
	 * @return new valid position
	 */
	default IVector2 getValidPositionByIcing(final IVector2 pos)
	{
		return pos;
	}
}
