/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Common methods for all defender roles
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IDefenderRole
{
	/**
	 * @param threatLine
	 * @return the current protection line
	 */
	ILineSegment getProtectionLine(final ILineSegment threatLine);
	
	
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
