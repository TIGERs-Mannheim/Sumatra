/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.MessagesRobocupSslGameEvent.SSL_Referee_Game_Event;
import edu.tigers.sumatra.MessagesRobocupSslGameEvent.SSL_Referee_Game_Event.Originator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class GameEvent implements IGameEvent
{
	private final EGameEvent eventType;
	private final long timestamp; // ns
	private final ETeamColor responsibleTeam;
	private final BotID responsibleBot;
	
	private final FollowUpAction followUpAction;
	private final List<CardPenalty> cardPenalties;
	
	private String cachedLogString = null;
	
	
	public GameEvent(final EGameEvent eventType, final long timestamp,
			final BotID responsibleBot, final FollowUpAction followUp)
	{
		this(eventType, timestamp, responsibleBot, followUp, Collections.emptyList());
	}
	
	
	public GameEvent(final EGameEvent eventType, final long timestamp,
			final BotID responsibleBot, final FollowUpAction followUp, final List<CardPenalty> cardPenalties)
	{
		this.eventType = eventType;
		this.timestamp = timestamp;
		this.responsibleTeam = responsibleBot.getTeamColor();
		this.responsibleBot = responsibleBot;
		
		this.followUpAction = followUp;
		this.cardPenalties = cardPenalties;
		
	}
	
	
	public GameEvent(final EGameEvent eventType, final long timestamp, final ETeamColor responsibleTeam,
			final FollowUpAction followUp)
	{
		this(eventType, timestamp, responsibleTeam, followUp, Collections.emptyList());
	}
	
	
	public GameEvent(final EGameEvent eventType, final long timestamp, final ETeamColor responsibleTeam,
			final FollowUpAction followUp, final List<CardPenalty> cardPenalties)
	{
		this.eventType = eventType;
		this.timestamp = timestamp;
		this.responsibleTeam = responsibleTeam;
		this.responsibleBot = null;
		
		this.followUpAction = followUp;
		this.cardPenalties = cardPenalties;
	}
	
	
	@Override
	public SSL_Referee_Game_Event toProtobuf()
	{
		SSL_Referee_Game_Event.Builder event = SSL_Referee_Game_Event.newBuilder();
		event.setGameEventType(eventType.getGameEventType());
		Originator.Builder originator = Originator.newBuilder();
		originator.setTeam(getOriginatingTeam());
		if (responsibleBot != null && responsibleBot.isBot())
		{
			originator.setBotId(responsibleBot.getNumber());
		}
		event.setOriginator(originator);
		return event.build();
	}
	
	
	private SSL_Referee_Game_Event.Team getOriginatingTeam()
	{
		if (responsibleTeam == ETeamColor.YELLOW)
		{
			return SSL_Referee_Game_Event.Team.TEAM_YELLOW;
		} else if (responsibleTeam == ETeamColor.BLUE)
		{
			return SSL_Referee_Game_Event.Team.TEAM_BLUE;
		}
		return SSL_Referee_Game_Event.Team.TEAM_UNKNOWN;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return eventType;
	}
	
	
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
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
	public synchronized String buildLogString()
	{
		if (cachedLogString == null)
		{
			cachedLogString = generateLogString();
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
	public List<CardPenalty> getCardPenalties()
	{
		return Collections.unmodifiableList(cardPenalties);
	}
}
