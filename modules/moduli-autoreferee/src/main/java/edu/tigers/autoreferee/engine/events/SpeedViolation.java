/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.text.DecimalFormat;
import java.util.List;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author "Lukas Magel"
 */
public class SpeedViolation extends GameEvent
{
	/** speed in m/s */
	private final double speed;
	
	
	public SpeedViolation(final EGameEvent eventType, final long timestamp, final BotID botAtFault,
			final FollowUpAction followUp, final double speed, final List<CardPenalty> cardPenalties)
	{
		super(eventType, timestamp, botAtFault, followUp, cardPenalties);
		this.speed = speed;
	}
	
	
	/**
	 * The speed value which lead to this violation
	 * 
	 * @return the speed in m/s
	 */
	public double getSpeed()
	{
		return speed;
	}
	
	
	@Override
	protected String generateLogString()
	{
		DecimalFormat format = new DecimalFormat("#.000");
		
		String superResult = super.generateLogString();
		StringBuilder builder = new StringBuilder(superResult);
		
		builder.append(" | Speed: ");
		builder.append(format.format(speed));
		builder.append("m/s");
		
		return builder.toString();
	}
}
