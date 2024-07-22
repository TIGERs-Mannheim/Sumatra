/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import com.sleepycat.persist.model.Persistent;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@Persistent
@AllArgsConstructor
public class InterceptionCorridor
{
	double startTime;
	double endTime;
	double width;


	@SuppressWarnings("unused") // berkeley
	private InterceptionCorridor()
	{
		startTime = 0;
		endTime = 0;
		width = 0;
	}
}
