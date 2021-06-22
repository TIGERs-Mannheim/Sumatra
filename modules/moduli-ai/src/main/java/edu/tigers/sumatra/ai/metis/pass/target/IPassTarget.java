/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.target;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A PassTarget is a position to which a ball can be passed.
 */
public interface IPassTarget
{
	/**
	 * @return the associated bot's id
	 */
	BotID getBotId();


	/**
	 * @return the target position as a {@Link IVector2}
	 */
	IVector2 getPos();
}
