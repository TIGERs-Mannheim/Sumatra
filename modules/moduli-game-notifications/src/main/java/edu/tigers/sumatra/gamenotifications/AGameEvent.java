/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamenotifications;

import edu.tigers.sumatra.Referee;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public abstract class AGameEvent
{
	
	private final EGameEvent				eventType;
	private final Referee.SSL_Referee	refMsg;
	
	
	protected AGameEvent(EGameEvent eventType, Referee.SSL_Referee refMsg)
	{
		
		this.eventType = eventType;
		this.refMsg = refMsg;
	}
	
	
	public EGameEvent getEventType()
	{
		
		return eventType;
	}
	
	
	public Referee.SSL_Referee getRefMsg()
	{
		
		return refMsg;
	}
}
