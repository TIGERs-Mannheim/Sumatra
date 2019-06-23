/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events;

import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class GameEvent implements IGameEvent
{
	private final EGameEvent		eventType;
	private final long				timestamp;						// ns
	private final ETeamColor		responsibleTeam;
	private final BotID				responsibleBot;
	
	private final FollowUpAction	followUpAction;
	private final CardPenalty		cardPenalty;
	
	private String						cachedLogString	= null;
	
	
	/**
	 * @param eventType
	 * @param timestamp in ns
	 * @param responsibleBot
	 * @param followUp
	 */
	public GameEvent(final EGameEvent eventType, final long timestamp,
			final BotID responsibleBot, final FollowUpAction followUp)
	{
		this(eventType, timestamp, responsibleBot, followUp, null);
	}
	
	
	/**
	 * @param eventType
	 * @param timestamp in ns
	 * @param responsibleBot
	 * @param followUp
	 * @param cardPenalty
	 */
	public GameEvent(final EGameEvent eventType, final long timestamp,
			final BotID responsibleBot, final FollowUpAction followUp, final CardPenalty cardPenalty)
	{
		this.eventType = eventType;
		this.timestamp = timestamp;
		this.responsibleBot = responsibleBot;
		
		followUpAction = followUp;
		this.cardPenalty = cardPenalty;
		
		responsibleTeam = responsibleBot.getTeamColor();
	}
	
	
	/**
	 * @param eventType
	 * @param timestamp
	 * @param responsibleTeam
	 * @param followUp
	 */
	public GameEvent(final EGameEvent eventType, final long timestamp, final ETeamColor responsibleTeam,
			final FollowUpAction followUp)
	{
		this(eventType, timestamp, responsibleTeam, followUp, null);
	}
	
	
	/**
	 * @param eventType
	 * @param timestamp
	 * @param responsibleTeam
	 * @param followUp
	 * @param cardPenalty
	 */
	public GameEvent(final EGameEvent eventType, final long timestamp, final ETeamColor responsibleTeam,
			final FollowUpAction followUp, final CardPenalty cardPenalty)
	{
		this.eventType = eventType;
		this.timestamp = timestamp;
		this.responsibleTeam = responsibleTeam;
		responsibleBot = null;
		
		followUpAction = followUp;
		this.cardPenalty = cardPenalty;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public EGameEvent getType()
	{
		return eventType;
	}
	
	
	@Override
	public EEventCategory getCategory()
	{
		return eventType.getCategory();
	}
	
	
	/**
	 * @return timestamp in ns
	 */
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ETeamColor getResponsibleTeam()
	{
		return responsibleTeam;
	}
	
	
	@Override
	public Optional<BotID> getResponsibleBot()
	{
		return Optional.ofNullable(responsibleBot);
	}
	
	
	@Override
	public String buildLogString()
	{
		if (cachedLogString == null)
		{
			synchronized (this)
			{
				if (cachedLogString == null)
				{
					cachedLogString = generateLogString();
				}
			}
		}
		return cachedLogString;
	}
	
	
	protected String generateLogString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getType());
		if (responsibleBot != null)
		{
			builder.append(" | Bot: ");
			builder.append(responsibleBot.getNumber());
			builder.append(" ");
			builder.append(responsibleBot.getTeamColor());
		} else
		{
			builder.append(" | Team: ");
			builder.append(responsibleTeam);
		}
		return builder.toString();
	}
	
	
	@Override
	public String toString()
	{
		return buildLogString();
	}
	
	
	/**
	 * @return the followUpAction
	 */
	@Override
	public FollowUpAction getFollowUpAction()
	{
		return followUpAction;
	}
	
	
	@Override
	public Optional<CardPenalty> getCardPenalty()
	{
		return Optional.ofNullable(cardPenalty);
	}
}
