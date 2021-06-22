/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;


/**
 * A target for passes and goal kicks with a rated score and an optional angle range
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RatedTarget implements IRatedTarget
{
	IVector2 target;
	double range;
	double score;


	/**
	 * @param target
	 * @param score
	 * @return
	 */
	public static RatedTarget ratedPoint(IVector2 target, double score)
	{
		return new RatedTarget(target, 0, score);
	}


	/**
	 * @param range
	 * @param score
	 * @return
	 */
	public static RatedTarget ratedRange(IVector2 target, double range, double score)
	{
		return new RatedTarget(target, range, score);
	}
}
