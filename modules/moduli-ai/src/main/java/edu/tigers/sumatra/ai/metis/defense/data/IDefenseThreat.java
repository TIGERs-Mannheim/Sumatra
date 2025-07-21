/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Optional;


/**
 * A threat for the defense.
 */
public interface IDefenseThreat
{
	/**
	 * The start of the line is at the threat source, e.g. for the BallThreat at the ball/receiver position and
	 * the end of the line is at the target, e.g. tbe middle of our goal
	 *
	 * @return the line of this threat that should be defended.
	 */
	ILineSegment getThreatLine();


	/**
	 * The start of the line is closer to the threat source, e.g. for the BallThreat at the ball/receiver position and
	 * the end of the line is closer to the target, e.g. tbe middle of our goal
	 *
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
	 * @return The threat rating. The higher the more dangerous is the threat
	 */
	double getThreatRating();

	/**
	 * @param threat
	 * @return true, if this is the same threat
	 */
	boolean sameAs(IDefenseThreat threat);
}
