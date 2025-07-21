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
public class BotTooFastInStop extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	double speed;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotTooFastInStop(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotTooFastInStop().getByTeam());
		this.bot = event.getBotTooFastInStop().getByBot();
		this.location = toVector(event.getBotTooFastInStop().getLocation());
		this.speed = event.getBotTooFastInStop().getSpeed();
	}


	/**
	 * @param bot
	 * @param location
	 * @param speed [m/s]
	 */
	public BotTooFastInStop(BotID bot, IVector2 location, double speed)
	{
		super(EGameEvent.BOT_TOO_FAST_IN_STOP);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.speed = speed;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_TOO_FAST_IN_STOP);
		builder.getBotTooFastInStopBuilder().setByTeam(getTeam(team)).setByBot(bot).setSpeed((float) speed)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s moved to fast during stop: vmax=%.2f @ %s", bot, team, speed,
				formatVector(location));
	}
}
