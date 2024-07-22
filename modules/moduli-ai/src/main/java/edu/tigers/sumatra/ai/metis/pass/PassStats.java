/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;


@RequiredArgsConstructor
public class PassStats
{
	/**
	 * First Map key is the originating zone.
	 * Second Map key is the target zone
	 */
	@Getter
	final Map<EOffensiveZone, Map<EOffensiveZone, ZonePassStats>> zonePassSuccessMap;

	@Getter
	@Setter
	int nPasses = 0;

	@Getter
	@Setter
	int successfulPasses = 0;

	@Getter
	@Setter
	int numPassLineReachedOnTime = 0;
}
