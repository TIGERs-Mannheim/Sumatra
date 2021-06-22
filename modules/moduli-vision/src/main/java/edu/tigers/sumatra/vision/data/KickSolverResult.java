/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;


/**
 * Result of a straight or chip kick solver.
 *
 * @author AndreR <andre@ryll.cc>
 */
@Value
@AllArgsConstructor
public class KickSolverResult
{
	IVector2 kickPosition;
	IVector3 kickVelocity;
	long kickTimestamp;
	IVector2 kickSpin;
	String solverName;


	/**
	 * @param kickPosition
	 * @param kickVelocity
	 * @param kickTimestamp
	 */
	public KickSolverResult(final IVector2 kickPosition, final IVector3 kickVelocity, final long kickTimestamp,
			String solverName)
	{
		this.kickPosition = kickPosition;
		this.kickVelocity = kickVelocity;
		this.kickTimestamp = kickTimestamp;
		this.kickSpin = null;
		this.solverName = solverName;
	}


	public Optional<IVector2> getKickSpin()
	{
		return Optional.ofNullable(kickSpin);
	}
}
