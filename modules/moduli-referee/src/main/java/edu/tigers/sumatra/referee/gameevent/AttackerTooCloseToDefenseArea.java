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
public class AttackerTooCloseToDefenseArea extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	double distance;
	IVector2 ballLocation;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AttackerTooCloseToDefenseArea(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAttackerTooCloseToDefenseArea().getByTeam());
		this.bot = event.getAttackerTooCloseToDefenseArea().getByBot();
		this.location = toVector(event.getAttackerTooCloseToDefenseArea().getLocation());
		this.distance = toDistance(event.getAttackerTooCloseToDefenseArea().getDistance());
		this.ballLocation = toVector(event.getAttackerTooCloseToDefenseArea().getBallLocation());
	}


	/**
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public AttackerTooCloseToDefenseArea(BotID bot, IVector2 location, double distance, IVector2 ballLocation)
	{
		super(EGameEvent.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
		this.ballLocation = ballLocation;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		builder.getAttackerTooCloseToDefenseAreaBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location))
				.setBallLocation(getLocationFromVector(ballLocation));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Attacker %d %s was %.2f mm away from the penalty area @ %s", bot, team, distance,
				formatVector(location));
	}
}
