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
public class BotTippedOver extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	IVector2 ballLocation;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotTippedOver(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotTippedOver().getByTeam());
		this.bot = event.getBotTippedOver().getByBot();
		this.location = toVector(event.getBotTippedOver().getLocation());
		this.ballLocation = toVector(event.getBotTippedOver().getBallLocation());
	}


	/**
	 * @param bot
	 * @param location
	 */
	public BotTippedOver(BotID bot, IVector2 location, IVector2 ballLocation)
	{
		super(EGameEvent.BOT_TIPPED_OVER);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.ballLocation = ballLocation;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_TIPPED_OVER);
		builder.getBotTippedOverBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setBallLocation(getLocationFromVector(ballLocation));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d of team %s tipped over @ %s", bot, team, location);
	}
}
