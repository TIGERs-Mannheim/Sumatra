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
public class AttackerTouchedOpponentInDefenseArea extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final int victim;
	private final IVector2 location;


	@SuppressWarnings("unsued") // used by berkeley
	protected AttackerTouchedOpponentInDefenseArea()
	{
		team = null;
		bot = 0;
		victim = 0;
		location = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AttackerTouchedOpponentInDefenseArea(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAttackerTouchedOpponentInDefenseArea().getByTeam());
		this.bot = event.getAttackerTouchedOpponentInDefenseArea().getByBot();
		this.victim = event.getAttackerTouchedOpponentInDefenseArea().getVictim();
		this.location = toVector(event.getAttackerTouchedOpponentInDefenseArea().getLocation());
	}


	public AttackerTouchedOpponentInDefenseArea(BotID bot, BotID victim, IVector2 location)
	{
		super(EGameEvent.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
	}


	AttackerTouchedOpponentInDefenseArea(EGameEvent type, BotID bot, BotID victim, IVector2 location)
	{
		super(type);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
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
		builder.getAttackerTouchedOpponentInDefenseAreaBuilder()
				.setByBot(bot)
				.setByTeam(getTeam(team))
				.setVictim(victim)
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String toString()
	{
		return String.format("Attacker %d %s touched opponent %d @ %s", bot, team, victim, formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final AttackerTouchedOpponentInDefenseArea that = (AttackerTouchedOpponentInDefenseArea) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(team, that.team)
				.append(victim, that.victim)
				.append(location, that.location)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(bot)
				.append(victim)
				.append(location)
				.toHashCode();
	}
}
