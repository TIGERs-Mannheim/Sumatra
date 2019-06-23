/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IDefenseThreat
{
	/**
	 * Get the line of this threat that should be defended.
	 * 
	 * @return
	 */
	ILineSegment getThreatLine();
	
	
	/**
	 * Get some arbitrary score of this threat.
	 * 
	 * @return
	 */
	double getScore();
	
	
	/**
	 * Threat position.
	 * 
	 * @return
	 */
	default IVector2 getPos()
	{
		return getThreatLine().getStart();
	}
	
	
	/**
	 * Velocity if this threat.
	 * 
	 * @return
	 */
	IVector2 getVel();
	
	
	/**
	 * Is this a bot?
	 * 
	 * @return
	 */
	boolean isBot();
}
