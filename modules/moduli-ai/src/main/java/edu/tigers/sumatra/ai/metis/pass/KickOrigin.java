/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;


/**
 * The origin from where the ball will be kicked next.
 * This is either the current ball position, if the ball is not moving,
 * or it is the location where the ball will be intercepted / catched / redirected.
 */
@Value
public class KickOrigin
{
	IVector2 pos;
	BotID shooter;
	/**
	 * The time until the ball is reaching the kick origin.
	 * If the ball is not moving, this value is Inf.
	 */
	double impactTime;


	public boolean isReached()
	{
		return Double.isInfinite(impactTime);
	}
}
