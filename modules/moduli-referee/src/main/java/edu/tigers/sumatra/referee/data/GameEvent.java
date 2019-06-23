/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.MessagesRobocupSslGameEvent;
import edu.tigers.sumatra.MessagesRobocupSslGameEvent.SSL_Referee_Game_Event.GameEventType;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * A autoRef game event from refBox
 */
@Persistent
public class GameEvent
{
	private final GameEventType gameEventType;
	private final ETeamColor originatorTeam;
	private final BotID originatorBot;
	private final String message;
	
	
	public GameEvent()
	{
		gameEventType = GameEventType.UNKNOWN;
		originatorTeam = null;
		originatorBot = null;
		message = "";
	}
	
	
	public GameEvent(MessagesRobocupSslGameEvent.SSL_Referee_Game_Event gameEvent)
	{
		if (gameEvent == null)
		{
			gameEventType = GameEventType.UNKNOWN;
			originatorTeam = null;
			originatorBot = null;
			message = "";
		} else
		{
			gameEventType = gameEvent.getGameEventType();
			if (gameEvent.hasOriginator())
			{
				originatorTeam = gameEvent.getOriginator()
						.getTeam() == MessagesRobocupSslGameEvent.SSL_Referee_Game_Event.Team.TEAM_BLUE ? ETeamColor.BLUE
								: ETeamColor.YELLOW;
				if (gameEvent.getOriginator().hasBotId())
				{
					originatorBot = BotID.createBotId(gameEvent.getOriginator().getBotId(), originatorTeam);
				} else
				{
					originatorBot = null;
				}
			} else
			{
				originatorTeam = null;
				originatorBot = null;
			}
			if (gameEvent.hasMessage())
			{
				message = gameEvent.getMessage();
			} else
			{
				message = "";
			}
		}
	}
	
	
	public GameEventType getGameEventType()
	{
		return gameEventType;
	}
	
	
	public Optional<ETeamColor> getOriginatorTeam()
	{
		return Optional.ofNullable(originatorTeam);
	}
	
	
	public Optional<BotID> getOriginatorBot()
	{
		return Optional.ofNullable(originatorBot);
	}
	
	
	public String getMessage()
	{
		return message;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(gameEventType);
		if (getOriginatorBot().isPresent() || getOriginatorTeam().isPresent())
		{
			sb.append(" by ");
			if (getOriginatorBot().isPresent())
			{
				sb.append(getOriginatorBot().get().toString());
			} else
			{
				sb.append(getOriginatorTeam().get().toString());
			}
		}
		if (!message.isEmpty())
		{
			sb.append('(');
			sb.append(message);
			sb.append(')');
		}
		return sb.toString();
	}
}
