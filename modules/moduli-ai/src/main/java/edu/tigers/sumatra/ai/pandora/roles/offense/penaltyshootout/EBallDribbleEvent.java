/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.penaltyshootout;

import edu.tigers.sumatra.statemachine.IEvent;


public enum EBallDribbleEvent implements IEvent
{
	DRIBBLING_FINISHED,
	DRIBBLING_FAILED
}
