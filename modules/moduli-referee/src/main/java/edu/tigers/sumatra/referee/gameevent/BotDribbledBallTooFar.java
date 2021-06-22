/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Persistent
@Value
@EqualsAndHashCode(callSuper = true)
public class BotDribbledBallTooFar extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 start;
	IVector2 end;


	@SuppressWarnings("unsued") // used by berkeley
	protected BotDribbledBallTooFar()
	{
		team = null;
		bot = 0;
		start = null;
		end = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotDribbledBallTooFar(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotDribbledBallTooFar().getByTeam());
		this.bot = event.getBotDribbledBallTooFar().getByBot();
		this.start = toVector(event.getBotDribbledBallTooFar().getStart());
		this.end = toVector(event.getBotDribbledBallTooFar().getEnd());
	}


	/**
	 * @param bot
	 * @param start
	 * @param end
	 */
	public BotDribbledBallTooFar(BotID bot, IVector2 start, IVector2 end)
	{
		super(EGameEvent.BOT_DRIBBLED_BALL_TOO_FAR);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.start = start;
		this.end = end;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_DRIBBLED_BALL_TOO_FAR);
		builder.getBotDribbledBallTooFarBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setStart(getLocationFromVector(start))
				.setEnd(getLocationFromVector(end));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s dribbled the ball %.2f mm (%s -> %s)", bot, team, start.distanceTo(end),
				formatVector(start),
				formatVector(end));
	}
}
