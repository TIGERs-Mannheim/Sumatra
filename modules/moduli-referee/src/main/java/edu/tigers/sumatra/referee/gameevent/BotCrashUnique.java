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
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class BotCrashUnique extends AGameEvent
{
	private final ETeamColor team;
	private final int violator;
	private final int victim;
	private final IVector2 location;
	private final double crashSpeed;
	private final double speedDiff;
	private final double crashAngle;


	@SuppressWarnings("unsued") // used by berkeley
	protected BotCrashUnique()
	{
		team = null;
		violator = 0;
		victim = 0;
		location = null;
		crashSpeed = 0;
		speedDiff = 0;
		crashAngle = 0;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotCrashUnique(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotCrashUnique().getByTeam());
		this.violator = event.getBotCrashUnique().getViolator();
		this.victim = event.getBotCrashUnique().getVictim();
		this.location = toVector(event.getBotCrashUnique().getLocation());
		this.crashSpeed = event.getBotCrashUnique().getCrashSpeed();
		this.speedDiff = event.getBotCrashUnique().getSpeedDiff();
		this.crashAngle = event.getBotCrashUnique().getCrashAngle();
	}


	/**
	 * @param violator
	 * @param victim
	 * @param location
	 * @param crashSpeed [m/s]
	 * @param speedDiff [m/s]
	 * @param crashAngle [rad]
	 */
	public BotCrashUnique(
			BotID violator,
			BotID victim,
			IVector2 location,
			double crashSpeed,
			double speedDiff,
			double crashAngle)
	{
		this(EGameEvent.BOT_CRASH_UNIQUE, violator, victim, location, crashSpeed, speedDiff, crashAngle);
	}


	BotCrashUnique(EGameEvent type,
			BotID violator,
			BotID victim,
			IVector2 location,
			double crashSpeed,
			double speedDiff,
			double crashAngle)
	{
		super(type);
		this.team = violator.getTeamColor();
		this.violator = violator.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
		this.crashSpeed = crashSpeed;
		this.speedDiff = speedDiff;
		this.crashAngle = crashAngle;
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
		builder.getBotCrashUniqueBuilder().setByTeam(getTeam(team)).setViolator(violator)
				.setVictim(victim).setCrashSpeed((float) crashSpeed).setSpeedDiff((float) speedDiff)
				.setCrashAngle((float) crashAngle).setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String toString()
	{
		return String.format("Bot %d %s crashed into bot %d %s with %.2f m/s @ %s (Δv: %.2f m/s, angle: %.0f°)",
				violator, team, victim, team.opposite(), crashSpeed, formatVector(location), speedDiff,
				AngleMath.rad2deg(crashAngle));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final BotCrashUnique that = (BotCrashUnique) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(violator, that.violator)
				.append(victim, that.victim)
				.append(crashSpeed, that.crashSpeed)
				.append(speedDiff, that.speedDiff)
				.append(crashAngle, that.crashAngle)
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
				.append(crashSpeed)
				.append(speedDiff)
				.append(crashAngle)
				.toHashCode();
	}
}
