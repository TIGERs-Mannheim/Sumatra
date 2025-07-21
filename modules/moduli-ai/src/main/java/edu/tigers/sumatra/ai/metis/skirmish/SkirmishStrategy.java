/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.skirmish;

import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


public record SkirmishStrategy(
		ESkirmishStrategyType type,
		List<IVector2> targetPositions
)
{
	private static final SkirmishStrategy NONE = new SkirmishStrategy(ESkirmishStrategyType.NONE, List.of());


	public static SkirmishStrategy none()
	{
		return NONE;
	}


	public int numRobots()
	{
		return targetPositions.size();
	}
}
