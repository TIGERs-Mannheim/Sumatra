/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate;

import edu.tigers.sumatra.statemachine.IEvent;


/**
 * @author MarkG
 */
public enum EKickStateEvent implements IEvent
{
	/**
	 * Switching to protection Mode
	 */
	PROTECT_BALL,
	/**
	 * Timeout.. may switch back to normal State here
	 */
	TIMED_OUT,
	/**
	 * good strategy has been found
	 */
	FOUND_GOOD_STRATEGY,
	/**
	 * Ball must be redirected or received
	 */
	CATCH_BALL,
	/**
	 * Ball can not be catched, because direction wrong or ball not moving
	 */
	CATCH_NOT_POSSIBLE
}
