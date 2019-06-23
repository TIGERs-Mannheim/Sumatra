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
public class YellowCardEvent extends AGameEvent
{
	private Referee.SSL_Referee.TeamInfo team;
	
	
	/**
	 * Creates a new YellowCardEvent
	 * 
	 * @param refMsg
	 * @param team
	 */
	public YellowCardEvent(final Referee.SSL_Referee refMsg, final Referee.SSL_Referee.TeamInfo team)
	{
		super(EGameEvent.YELLOW_CARD, refMsg);
		
		this.team = team;
	}
	
	
	public Referee.SSL_Referee.TeamInfo getTeam()
	{
		return team;
	}
}
