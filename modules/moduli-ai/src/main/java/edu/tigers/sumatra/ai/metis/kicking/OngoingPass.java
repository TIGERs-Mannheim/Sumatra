/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;


/**
 * Stores information about a pass that is already on its way.
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class OngoingPass
{
	@NonNull
	Pass pass;
	long kickStartTime;

	EOffensiveZone originatingZone;
	EOffensiveZone targetZone;

	boolean passLineBeenReachedByReceiver;
}
