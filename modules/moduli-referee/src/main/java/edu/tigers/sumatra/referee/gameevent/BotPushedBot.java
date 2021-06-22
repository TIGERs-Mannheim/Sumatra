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
public class BotPushedBot extends AGameEvent
{
	ETeamColor team;
	int violator;
	int victim;
	IVector2 location;
	double pushedDistance;


	@SuppressWarnings("unsued") // used by berkeley
	protected BotPushedBot()
	{
		team = null;
		violator = 0;
		victim = 0;
		location = null;
		pushedDistance = 0;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotPushedBot(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotPushedBot().getByTeam());
		this.violator = event.getBotPushedBot().getViolator();
		this.victim = event.getBotPushedBot().getVictim();
		this.location = toVector(event.getBotPushedBot().getLocation());
		this.pushedDistance = toDistance(event.getBotPushedBot().getPushedDistance());
	}


	/**
	 * @param violator
	 * @param victim
	 * @param location
	 * @param pushedDistance [mm]
	 */
	public BotPushedBot(
			BotID violator,
			BotID victim,
			IVector2 location,
			double pushedDistance)
	{
		this(EGameEvent.BOT_PUSHED_BOT, violator, victim, location, pushedDistance);
	}


	BotPushedBot(
			EGameEvent type,
			BotID violator,
			BotID victim,
			IVector2 location,
			double pushedDistance)
	{
		super(type);
		this.team = violator.getTeamColor();
		this.violator = violator.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
		this.pushedDistance = pushedDistance;
	}


	public IVector2 getLocation()
	{
		return location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(getType().getProtoType());
		builder.getBotPushedBotBuilder().setByTeam(getTeam(team)).setViolator(violator)
				.setVictim(victim).setPushedDistance((float) pushedDistance / 1000.f)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s pushed bot %d %s for %.2f mm @ %s", violator, team, victim, team.opposite(),
				pushedDistance, formatVector(location));
	}
}
