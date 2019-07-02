/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class BotPushedBot extends AGameEvent
{
	private final ETeamColor team;
	private final int violator;
	private final int victim;
	private final IVector2 location;
	private final double pushedDistance;


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
	public BotPushedBot(SslGameEvent.GameEvent event)
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
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(getType().getProtoType());
		builder.getBotPushedBotBuilder().setByTeam(getTeam(team)).setViolator(violator)
				.setVictim(victim).setPushedDistance((float) pushedDistance / 1000.f)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String toString()
	{
		return String.format("Bot %d %s pushed bot %d %s for %.2f mm @ %s", violator, team, victim, team.opposite(),
				pushedDistance, formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final BotPushedBot that = (BotPushedBot) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(violator, that.violator)
				.append(victim, that.victim)
				.append(pushedDistance, that.pushedDistance)
				.append(team, that.team)
				.append(location, that.location)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(violator)
				.append(victim)
				.append(location)
				.append(pushedDistance)
				.toHashCode();
	}
}
