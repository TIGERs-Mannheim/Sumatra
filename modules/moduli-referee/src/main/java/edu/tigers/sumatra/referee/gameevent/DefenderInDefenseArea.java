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
public class DefenderInDefenseArea extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;
	double distance;


	@SuppressWarnings("unsued") // used by berkeley
	protected DefenderInDefenseArea()
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
	public DefenderInDefenseArea(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getDefenderInDefenseArea().getByTeam());
		this.bot = event.getDefenderInDefenseArea().getByBot();
		this.location = toVector(event.getDefenderInDefenseArea().getLocation());
		this.distance = toDistance(event.getDefenderInDefenseArea().getDistance());
	}


	public DefenderInDefenseArea(BotID bot, IVector2 location, double distance)
	{
		super(EGameEvent.DEFENDER_IN_DEFENSE_AREA);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();

		builder.setType(SslGcGameEvent.GameEvent.Type.DEFENDER_IN_DEFENSE_AREA);
		builder.getDefenderInDefenseAreaBuilder()
				.setByTeam(getTeam(team))
				.setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Defender %s %d was fully in the defense area with %.2f mm @ %s", team, bot,
				distance, formatVector(location));
	}
}
