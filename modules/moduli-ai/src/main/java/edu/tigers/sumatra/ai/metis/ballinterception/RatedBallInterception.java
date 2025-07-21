/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RatedBallInterception
{
	BallInterception ballInterception;
	double corridorLength;
	double minCorridorSlackTime;
}
