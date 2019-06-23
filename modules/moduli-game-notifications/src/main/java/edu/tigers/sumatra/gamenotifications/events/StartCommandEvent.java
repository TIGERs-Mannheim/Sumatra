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
public class StartCommandEvent extends AGameEvent
{

     /**
	 * The game start's type
	 */
	public enum StartType
	{
		NORMAL,
		FORCE
	}
	
	private StartType type = null;
	
	
	/**
	 * Creates a new StartCommandEvent
	 * 
	 * @param type
	 * @param refMsg
	 */
	public StartCommandEvent(StartType type, Referee.SSL_Referee refMsg)
	{
		
		super(EGameEvent.START_COMMAND, refMsg);
		
		this.type = type;
	}

    public StartType getType() {
        return type;
    }
}
