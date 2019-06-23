/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Data class to make scoring of different areas in
 * the enemy half during penalty shootout possible.
 * <i>Note: This class has a natural ordering that is inconsistent with equals</i>
 * 
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
@Persistent
public class PenaltyPlacementTargetGroup extends Circle
{
	
	private int attempts = 0;
	private int successfulAttempts = 0;
	
	
	@SuppressWarnings("unused")
	private PenaltyPlacementTargetGroup()
	{
		// empty for berkeley
	}
	
	
	/**
	 * Creates a new PenaltyPlacementTargetGroup
	 * 
	 * @param center
	 * @param radius
	 */
	public PenaltyPlacementTargetGroup(final IVector2 center, final double radius)
	{
		super(center, radius);
	}
	
	
	public int getAttempts()
	{
		return attempts;
	}
	
	
	public void setAttempts(final int attempts)
	{
		this.attempts = attempts;
	}
	
	
	public int getSuccessfulAttempts()
	{
		return successfulAttempts;
	}
	
	
	public void setSuccessfulAttempts(final int successfulAttempts)
	{
		this.successfulAttempts = successfulAttempts;
	}
	
	
	/**
	 * Calculates the score of the current group
	 *
	 * @return
	 */
	public int calculateScore()
	{
		int failedAttempts = getAttempts() - getSuccessfulAttempts();
		return getSuccessfulAttempts() - failedAttempts;
	}
}
