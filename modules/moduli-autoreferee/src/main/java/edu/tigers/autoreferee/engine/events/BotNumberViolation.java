/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class BotNumberViolation extends GameEvent
{
	
	private final int	allowedNumber;
	private final int	actualNumber;
	
	
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
		super(EGameEvent.BOT_COUNT, timestamp, responsibleTeam, followUp);
		this.allowedNumber = allowedNumber;
		this.actualNumber = actualNumber;
	}
	
	
	/**
	 * @return the allowedNumber
	 */
	public int getAllowedNumber()
	{
		return allowedNumber;
	}
	
	
	/**
	 * @return the actualNumber
	 */
	public int getActualNumber()
	{
		return actualNumber;
	}
	
	
	@Override
	public String buildLogString()
	{
		return super.buildLogString();
	}
	
	
	@Override
	protected String generateLogString()
	{
		String superResult = super.generateLogString();
		StringBuilder builder = new StringBuilder(superResult);
		
		builder.append(" | Allowed/Actual: ");
		builder.append(getAllowedNumber());
		builder.append("/");
		builder.append(getActualNumber());
		
		return builder.toString();
	}
	
}
