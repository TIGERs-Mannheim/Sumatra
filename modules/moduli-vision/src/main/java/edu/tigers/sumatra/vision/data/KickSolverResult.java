/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * Result of a straight or chip kick solver.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class KickSolverResult
{
	private final IVector2 kickPosition;
	private final IVector3 kickVelocity;
	private final long kickTimestamp;
	
	
	/**
	 * @param kickPosition
	 * @param kickVelocity
	 * @param kickTimestamp
	 */
	public KickSolverResult(final IVector2 kickPosition, final IVector3 kickVelocity, final long kickTimestamp)
	{
		this.kickPosition = kickPosition;
		this.kickVelocity = kickVelocity;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	/**
	 * @return the kickPosition
	 */
	public IVector2 getKickPosition()
	{
		return kickPosition;
	}
	
	
	/**
	 * @return the kickVelocity
	 */
	public IVector3 getKickVelocity()
	{
		return kickVelocity;
	}
	
	
	/**
	 * @return the kickTimestamp
	 */
	public long getKickTimestamp()
	{
		return kickTimestamp;
	}
}
