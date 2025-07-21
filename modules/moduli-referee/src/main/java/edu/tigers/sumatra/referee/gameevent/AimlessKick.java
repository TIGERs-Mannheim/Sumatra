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
public class AimlessKick extends AGameEvent
{
	ETeamColor team;
	Integer bot;
	IVector2 location;
	IVector2 kickLocation;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AimlessKick(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAimlessKick().getByTeam());
		this.bot = event.getAimlessKick().getByBot();
		this.location = toVector(event.getAimlessKick().getLocation());
		this.kickLocation = toVector(event.getAimlessKick().getKickLocation());
	}


	public AimlessKick(BotID bot, IVector2 location, IVector2 kickLocation)
	{
		super(EGameEvent.AIMLESS_KICK);
		this.team = bot == null ? null : bot.getTeamColor();
		this.bot = bot == null ? null : bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.AIMLESS_KICK);
		builder.getAimlessKickBuilder()
				.setLocation(getLocationFromVector(location));

		if (bot != null)
		{
			builder.getAimlessKickBuilder().setByBot(bot);
		}

		if (team != null)
		{
			builder.getAimlessKickBuilder().setByTeam(getTeam(team));
		}

		if (kickLocation != null)
		{
			builder.getAimlessKickBuilder().setKickLocation(getLocationFromVector(kickLocation));
		}

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Aimless kick by Bot %d %s @ %s", bot, team, formatVector(location));
	}
}
