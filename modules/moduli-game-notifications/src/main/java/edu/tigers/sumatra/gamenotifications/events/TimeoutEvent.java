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
public class TimeoutEvent extends AGameEvent
{
	
	private Referee.SSL_Referee.TeamInfo team = null;
	
	
	/**
	 * Creates a new TimeoutEvent
	 * 
	 * @param refMsg
	 */
	public TimeoutEvent(final Referee.SSL_Referee refMsg)
	{
		
		super(EGameEvent.TIMEOUT, refMsg);
		
		switch (refMsg.getCommand())
		{
			case TIMEOUT_BLUE:
				team = getRefMsg().getBlue();
				break;
			case TIMEOUT_YELLOW:
				team = getRefMsg().getYellow();
				break;
			default:
				break;
		}
	}
	
	
	public Referee.SSL_Referee.TeamInfo getTeam()
	{
		
		return team;
	}
}
