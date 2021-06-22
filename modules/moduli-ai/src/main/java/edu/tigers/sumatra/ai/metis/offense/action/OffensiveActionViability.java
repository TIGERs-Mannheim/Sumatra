/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import com.sleepycat.persist.model.Persistent;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;


@Value
@Persistent
@AllArgsConstructor
public class OffensiveActionViability
{
	@NonNull
	EActionViability type;
	/**
	 * viability score [0,1]. 1 is good.
	 */
	double score;


	@SuppressWarnings("unused") // used by berkeley
	private OffensiveActionViability()
	{
		type = EActionViability.FALSE;
		score = 0;
	}
}
