/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;
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


	@SuppressWarnings("unused") // berkeley
	private RatedBallInterception()
	{
		corridorLength = 0;
		minCorridorSlackTime = 0;
		ballInterception = new BallInterception(BotID.noBot(), 0, Vector2.zero());
	}

}
