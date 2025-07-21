/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class InterceptionIteration
{
	double ballTravelTime;
	double slackTime;
	double includedSlackTimeBonus;
	boolean isPreferred;
	double distanceAtOvershoot;
	double velocityAtOvershoot;
}
