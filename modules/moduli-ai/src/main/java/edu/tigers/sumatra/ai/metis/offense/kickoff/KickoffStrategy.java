/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.kickoff;

import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;


/**
 * Kickoff strategy parameters
 */
@Value
@AllArgsConstructor
public class KickoffStrategy
{
	Pass bestPass;
	Kick kick;
	List<IVector2> bestMovementPositions;


	public KickoffStrategy()
	{
		bestPass = null;
		kick = null;
		bestMovementPositions = Collections.emptyList();
	}
}
