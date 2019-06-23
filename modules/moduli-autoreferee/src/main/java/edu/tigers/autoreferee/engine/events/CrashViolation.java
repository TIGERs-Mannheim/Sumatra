/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import java.text.DecimalFormat;
import java.util.List;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.BotID;


public class CrashViolation extends GameEvent
{
	private static final DecimalFormat DF = new DecimalFormat("0.000");
	
	private BotID secondResponsibleBot;
	private double collisionSpeed;
	private double speedDifference;
	private double speedPrimaryBot;
	private double speedSecondaryBot;
	
	
	public CrashViolation(
			final EGameEvent eventType,
			final long timestamp,
			final BotID responsibleBot,
			final double collisionSpeed,
			final double speedDifference,
			final FollowUpAction followUp,
			final List<CardPenalty> cardPenalties)
	{
		super(eventType, timestamp, responsibleBot, followUp, cardPenalties);
		this.collisionSpeed = collisionSpeed;
		this.speedDifference = speedDifference;
	}
	
	
	public CrashViolation setSecondResponsibleBot(final BotID secondResponsibleBot)
	{
		this.secondResponsibleBot = secondResponsibleBot;
		return this;
	}
	
	
	public CrashViolation setSpeedPrimaryBot(final double speedPrimaryBot)
	{
		this.speedPrimaryBot = speedPrimaryBot;
		return this;
	}
	
	
	public CrashViolation setSpeedSecondaryBot(final double speedSecondaryBot)
	{
		this.speedSecondaryBot = speedSecondaryBot;
		return this;
	}
	
	
	@Override
	protected String generateLogString()
	{
		if (secondResponsibleBot != null)
		{
			return generateBothTeamsViolationString();
		}
		return generateSingleBotViolationString();
	}
	
	
	private String generateSingleBotViolationString()
	{
		return super.generateLogString()
				+ " | CollisionSpeed: "
				+ DF.format(collisionSpeed)
				+ "m/s"
				+ " | SpeedDifference: "
				+ DF.format(speedDifference)
				+ "m/s"
				+ " | SpeedPrimary: "
				+ DF.format(speedPrimaryBot)
				+ "m/s"
				+ " | SpeedSecondary: "
				+ DF.format(speedSecondaryBot)
				+ "m/s";
	}
	
	
	private String generateBothTeamsViolationString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getType());
		if (getResponsibleBot().isPresent())
		{
			builder.append(" | Bots: ");
			builder.append(getResponsibleBot().get().getNumber());
			builder.append(" ");
			builder.append(getResponsibleBot().get().getTeamColor());
			
			builder.append(" & ");
			builder.append(secondResponsibleBot.getNumber());
			builder.append(" ");
			builder.append(secondResponsibleBot.getTeamColor());
			
			builder.append(" | CollisionSpeed: ");
			builder.append(DF.format(collisionSpeed));
			builder.append("m/s");
			
			builder.append(" | SpeedDifference: ");
			builder.append(DF.format(speedDifference));
			builder.append("m/s");
			
			builder.append(" | SpeedPrimary: ");
			builder.append(DF.format(speedPrimaryBot));
			builder.append("m/s");
			
			builder.append(" | SpeedSecondary: ");
			builder.append(DF.format(speedSecondaryBot));
			builder.append("m/s");
		}
		return builder.toString();
	}
}
