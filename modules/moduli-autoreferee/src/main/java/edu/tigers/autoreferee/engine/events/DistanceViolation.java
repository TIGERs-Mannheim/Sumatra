/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author "Lukas Magel"
 */
public class DistanceViolation extends GameEvent
{
	/** speed in m/s */
	private final double distance;
	
	
	/**
	 * @param eventType
	 * @param timestamp
	 * @param botAtFault
	 * @param followUp
	 * @param distance in mm
	 */
	public DistanceViolation(final EGameEvent eventType, final long timestamp, final BotID botAtFault,
			final FollowUpAction followUp, final double distance)
	{
		this(eventType, timestamp, botAtFault, followUp, null, distance);
	}
	
	
	/**
	 * @param eventType
	 * @param timestamp
	 * @param botAtFault
	 * @param followUp
	 * @param cardPenalty
	 * @param distance in mm
	 */
	public DistanceViolation(final EGameEvent eventType, final long timestamp, final BotID botAtFault,
			final FollowUpAction followUp, final CardPenalty cardPenalty, final double distance)
	{
		super(eventType, timestamp, botAtFault, followUp,
				Optional.ofNullable(cardPenalty).map(Collections::singletonList).orElseGet(Collections::emptyList));
		this.distance = distance;
	}
	
	
	/**
	 * @return the distance
	 */
	public double getDistance()
	{
		return distance;
	}
	
	
	@Override
	protected String generateLogString()
	{
		DecimalFormat format = new DecimalFormat("####.0");
		
		String superResult = super.generateLogString();
		StringBuilder builder = new StringBuilder(superResult);
		
		builder.append(" | Distance: ");
		builder.append(format.format(distance));
		builder.append("mm");
		
		return builder.toString();
	}
}
