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
public class DefenderTooCloseToKickPoint extends AGameEvent
{
	ETeamColor team;
	int bot;
	 IVector2 location;
	double distance;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public DefenderTooCloseToKickPoint(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getDefenderTooCloseToKickPoint().getByTeam());
		this.bot = event.getDefenderTooCloseToKickPoint().getByBot();
		this.location = toVector(event.getDefenderTooCloseToKickPoint().getLocation());
		this.distance = toDistance(event.getDefenderTooCloseToKickPoint().getDistance());
	}


	/**
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public DefenderTooCloseToKickPoint(BotID bot, IVector2 location, double distance)
	{
		super(EGameEvent.DEFENDER_TOO_CLOSE_TO_KICK_POINT);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.DEFENDER_TOO_CLOSE_TO_KICK_POINT);
		builder.getDefenderTooCloseToKickPointBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Defender %d %s was too close to the kick point: %.2f mm @ %s", bot, team, distance,
				formatVector(location));
	}
}
