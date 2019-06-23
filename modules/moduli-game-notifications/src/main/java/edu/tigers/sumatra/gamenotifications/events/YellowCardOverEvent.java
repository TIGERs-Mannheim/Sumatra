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
public class YellowCardOverEvent extends AGameEvent
{
	private Referee.SSL_Referee.TeamInfo team;
	
	
	/**
	 * Creates a new YellowCardOverEvent
	 * 
	 * @param refMsg
	 * @param teamInfo
	 */
	public YellowCardOverEvent(final Referee.SSL_Referee refMsg, Referee.SSL_Referee.TeamInfo teamInfo)
	{
		super(EGameEvent.YELLOW_CARD_OVER, refMsg);
		
		this.team = teamInfo;
	}
	
	
	public Referee.SSL_Referee.TeamInfo getTeam()
	{
		return team;
	}
}
