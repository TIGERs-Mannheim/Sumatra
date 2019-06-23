/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * A target for passes and goal kicks with a rated score and an optional angle range
 */
public final class RatedTarget implements IRatedTarget
{
	private final DynamicPosition target;
	private final double range;
	private final double score;
	
	
	private RatedTarget(final IVector2 target, final double range, final double score)
	{
		this.target = new DynamicPosition(target, range);
		this.range = range;
		this.score = score;
	}
	
	
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
	
	
	@Override
	public DynamicPosition getTarget()
	{
		return target;
	}
	
	
	@Override
	public double getRange()
	{
		return range;
	}
	
	
	@Override
	public double getScore()
	{
		return score;
	}
}
