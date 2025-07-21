/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;


@Value
@Builder(toBuilder = true)
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
}
