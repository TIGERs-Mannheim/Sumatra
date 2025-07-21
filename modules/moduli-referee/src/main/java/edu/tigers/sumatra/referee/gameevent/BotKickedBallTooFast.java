/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@EqualsAndHashCode(callSuper = true)
public class BotKickedBallTooFast extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	double initialBallSpeed;
	EKickType kickType;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotKickedBallTooFast(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotKickedBallTooFast().getByTeam());
		this.bot = event.getBotKickedBallTooFast().getByBot();
		this.location = toVector(event.getBotKickedBallTooFast().getLocation());
		this.initialBallSpeed = event.getBotKickedBallTooFast().getInitialBallSpeed();
		this.kickType = event.getBotKickedBallTooFast().getChipped() ? EKickType.CHIPPED : EKickType.STRAIGHT;
	}


	/**
	 * @param bot
	 * @param location
	 * @param initialBallSpeed [m/s]
	 * @param kickType
	 */
	public BotKickedBallTooFast(BotID bot, IVector2 location, double initialBallSpeed, EKickType kickType)
	{
		super(EGameEvent.BOT_KICKED_BALL_TOO_FAST);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.initialBallSpeed = initialBallSpeed;
		this.kickType = kickType;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_KICKED_BALL_TOO_FAST);
		builder.getBotKickedBallTooFastBuilder()
				.setByTeam(getTeam(team))
				.setByBot(bot)
				.setInitialBallSpeed((float) initialBallSpeed)
				.setChipped(kickType == EKickType.CHIPPED)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s %s ball too fast: vmax=%.2f m/s @ %s",
				bot, team, kickType == EKickType.STRAIGHT ? "kicked" : "chipped", initialBallSpeed,
				formatVector(location));
	}

	public enum EKickType
	{
		STRAIGHT,
		CHIPPED
	}
}
