/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamenotifications.events;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.gamenotifications.AGameEvent;
import edu.tigers.sumatra.gamenotifications.EGameEvent;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class HalfTimeEvent extends AGameEvent
{
	
	/**
	 * Creates a new HalfTimeEvent
	 * 
	 * @param refMsg
	 */
	public HalfTimeEvent(final Referee.SSL_Referee refMsg)
	{
		
		super(EGameEvent.HALF_TIME, refMsg);
	}
}
