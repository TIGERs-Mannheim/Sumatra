/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import com.sleepycat.persist.model.Persistent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;


@Value
@Builder(toBuilder = true)
@Persistent(version = 3)
@AllArgsConstructor
public class BallInterceptionInformation
{
	@NonNull
	List<InterceptionIteration> initialIterations;
	@NonNull
	List<InterceptionZeroAxisCrossing> zeroAxisChanges;
	@NonNull
	List<InterceptionCorridor> interceptionCorridors;
	double interceptionTargetTime;
	InterceptionIteration oldInterception;


	@SuppressWarnings("unused") // berkeley
	private BallInterceptionInformation()
	{
		initialIterations = Collections.emptyList();
		zeroAxisChanges = Collections.emptyList();
		interceptionCorridors = Collections.emptyList();
		interceptionTargetTime = 0;
		oldInterception = null;
	}
}
