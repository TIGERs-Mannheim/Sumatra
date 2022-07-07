/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import com.sleepycat.persist.model.Persistent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@Persistent
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RatedInterceptionIteration
{
	InterceptionIteration iteration;
	double corridorLength;
	double minCorridorSlackTime;


	@SuppressWarnings("unused") // berkeley
	private RatedInterceptionIteration()
	{
		corridorLength = 0;
		minCorridorSlackTime = 0;
		iteration = new InterceptionIteration(0, 0, 0);
	}
}
