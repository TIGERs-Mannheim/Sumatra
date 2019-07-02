/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.Optional;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A threat for the defense.
 */
public interface IDefenseThreat
{
	/**
	 * @return the line of this threat that should be defended.
	 */
	ILineSegment getThreatLine();


	/**
	 * @return the line of this threat on which the threat can be protected.
	 */
	Optional<ILineSegment> getProtectionLine();


	/**
	 * @return Current position of this threat.
	 */
	IVector2 getPos();


	/**
	 * @return Current velocity of this threat
	 */
	IVector2 getVel();


	/**
	 * @return the object id of this threat
	 */
	AObjectID getObjectId();


	/**
	 * @return the type of this threat
	 */
	EDefenseThreatType getType();


	/**
	 * @param threat
	 * @return true, if this is the same threat
	 */
	boolean sameAs(IDefenseThreat threat);
}
