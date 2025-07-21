/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;


@Value
@AllArgsConstructor
public class OffensiveActionViability
{
	@NonNull
	EActionViability type;
	/**
	 * viability score [0,1]. 1 is good.
	 */
	double score;
}
