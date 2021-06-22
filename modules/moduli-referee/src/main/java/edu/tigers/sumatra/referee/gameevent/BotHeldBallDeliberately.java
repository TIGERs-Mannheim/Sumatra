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
public class BotHeldBallDeliberately extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	double duration;


	@SuppressWarnings("unsued") // used by berkeley
	protected BotHeldBallDeliberately()
	{
		team = null;
		bot = 0;
		location = null;
		duration = 0;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotHeldBallDeliberately(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotHeldBallDeliberately().getByTeam());
		this.bot = event.getBotHeldBallDeliberately().getByBot();
		this.location = toVector(event.getBotHeldBallDeliberately().getLocation());
		this.duration = event.getBotHeldBallDeliberately().getDuration();
	}


	/**
	 * @param bot
	 * @param location
	 * @param duration [s]
	 */
	public BotHeldBallDeliberately(BotID bot, IVector2 location, double duration)
	{
		super(EGameEvent.BOT_HELD_BALL_DELIBERATELY);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.duration = duration;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_HELD_BALL_DELIBERATELY);
		builder.getBotHeldBallDeliberatelyBuilder().setByBot(bot).setByTeam(getTeam(team)).setDuration((float) duration)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s hold the ball deliberately for %.2f s @ %s", bot, team, duration,
				formatVector(location));
	}
}
