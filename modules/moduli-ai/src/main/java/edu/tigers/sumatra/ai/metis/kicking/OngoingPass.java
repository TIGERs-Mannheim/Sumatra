/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;


/**
 * Stores information about a pass that is already on its way.
 */
@Persistent
@Value
@Builder
@AllArgsConstructor
public class OngoingPass
{
	@NonNull
	Pass pass;
	long kickStartTime;

	EOffensiveZone originatingZone;
	EOffensiveZone targetZone;

	@SuppressWarnings("unused") // berkeley
	private OngoingPass()
	{
		pass = new Pass();
		kickStartTime = 0;
		originatingZone = null;
		targetZone = null;
	}
}
