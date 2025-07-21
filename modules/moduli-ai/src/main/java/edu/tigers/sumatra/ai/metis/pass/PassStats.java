/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Builder(toBuilder = true)
public class PassStats
{
	/**
	 * First Map key is the originating zone.
	 * Second Map key is the target zone
	 */
	@Getter
	final Map<EOffensiveZone, Map<EOffensiveZone, ZonePassStats>> zonePassSuccessMap;

	@Getter
	final List<PassStatsKickReceiveVelocity> receiveVelocities;

	@Getter
	final int nPasses;

	@Getter
	final int successfulPasses;

	@Getter
	final int numPassLineReachedOnTime;
}
