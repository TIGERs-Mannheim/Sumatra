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


@Persistent(version = 1)
@Value
@EqualsAndHashCode(callSuper = true)
public class AttackerTouchedBallInDefenseArea extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	double distance;


	@SuppressWarnings("unsued") // used by berkeley
	protected AttackerTouchedBallInDefenseArea()
	{
		team = null;
		bot = 0;
		location = null;
		distance = 0;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AttackerTouchedBallInDefenseArea(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAttackerTouchedBallInDefenseArea().getByTeam());
		this.bot = event.getAttackerTouchedBallInDefenseArea().getByBot();
		this.location = toVector(event.getAttackerTouchedBallInDefenseArea().getLocation());
		this.distance = toDistance(event.getAttackerTouchedBallInDefenseArea().getDistance());
	}


	/**
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public AttackerTouchedBallInDefenseArea(BotID bot, IVector2 location, double distance)
	{
		super(EGameEvent.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA);
		builder.getAttackerTouchedBallInDefenseAreaBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Attacker %d %s was %.2f mm in the defense Area @ %s", bot, team, distance,
				formatVector(location));
	}
}
