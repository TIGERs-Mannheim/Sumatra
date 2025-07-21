/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;


@Value
@Builder
@AllArgsConstructor
public class SupportBehaviorPosition
{
	IVector2 position;
	IVector2 lookAt;
	double viability;


	public SupportBehaviorPosition()
	{
		position = null;
		lookAt = null;
		viability = 0;
	}


	public Optional<IVector2> getLookAt()
	{
		return Optional.ofNullable(lookAt);
	}


	public static SupportBehaviorPosition notAvailable()
	{
		return new SupportBehaviorPosition(null, null, 0.0);
	}


	public static SupportBehaviorPosition fromDestination(IVector2 destination, double viability)
	{
		return new SupportBehaviorPosition(destination, null, viability);
	}


	public static SupportBehaviorPosition fromDestinationAndRotationTarget(
			IVector2 destination,
			IVector2 lookAt,
			double viability
	)
	{
		return new SupportBehaviorPosition(destination, lookAt, viability);
	}
}