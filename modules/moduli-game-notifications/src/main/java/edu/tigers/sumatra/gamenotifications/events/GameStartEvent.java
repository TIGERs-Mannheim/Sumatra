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
public class GameStartEvent extends AGameEvent
{
	
	private Referee.SSL_Referee.TeamInfo	blue		= null;
	private Referee.SSL_Referee.TeamInfo	yellow	= null;
	
	
	/**
	 * Creates a new GameStartEvent
	 * 
	 * @param refMsg
	 */
	public GameStartEvent(Referee.SSL_Referee refMsg)
	{
		
		super(EGameEvent.GAME_STARTS, refMsg);

		this.blue = refMsg.getBlue();
		this.yellow = refMsg.getYellow();
	}
	
	
	public Referee.SSL_Referee.TeamInfo getBlue()
	{
		return blue;
	}
	
	
	public Referee.SSL_Referee.TeamInfo getYellow()
	{
		return yellow;
	}
}
