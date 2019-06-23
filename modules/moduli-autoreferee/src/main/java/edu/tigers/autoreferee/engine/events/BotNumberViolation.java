/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class BotNumberViolation extends GameEvent
{
	
	private final int allowedNumber;
	private final int actualNumber;
	
	
	/**
	 * @param timestamp
	 * @param responsibleTeam
	 * @param followUp
	 * @param allowedNumber
	 * @param actualNumber
	 */
	public BotNumberViolation(final long timestamp, final ETeamColor responsibleTeam, final FollowUpAction followUp,
			final int allowedNumber, final int actualNumber)
	{
		super(EGameEvent.NUMBER_OF_PLAYERS, timestamp, responsibleTeam, followUp);
		this.allowedNumber = allowedNumber;
		this.actualNumber = actualNumber;
	}
	
	
	@Override
	protected String generateLogString()
	{
		String superResult = super.generateLogString();
		
		return superResult
				+ " | Allowed/Actual: "
				+ allowedNumber
				+ "/"
				+ actualNumber;
	}
	
}
