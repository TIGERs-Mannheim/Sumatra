/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class InterceptionCorridor
{
	double startTime;
	double endTime;
	double width;
}
