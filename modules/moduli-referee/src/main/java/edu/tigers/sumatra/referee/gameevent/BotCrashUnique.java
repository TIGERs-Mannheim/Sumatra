/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@EqualsAndHashCode(callSuper = true)
public class BotCrashUnique extends AGameEvent
{
	ETeamColor team;
	int violator;
	int victim;
	IVector2 location;
	double crashSpeed;
	double speedDiff;
	double crashAngle;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotCrashUnique(SslGcGameEvent.GameEvent event)
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
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(getType().getProtoType());
		builder.getBotCrashUniqueBuilder().setByTeam(getTeam(team)).setViolator(violator)
				.setVictim(victim).setCrashSpeed((float) crashSpeed).setSpeedDiff((float) speedDiff)
				.setCrashAngle((float) crashAngle).setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s crashed into bot %d %s with %.2f m/s @ %s (Δv: %.2f m/s, angle: %.0f°)",
				violator, team, victim, team.opposite(), crashSpeed, formatVector(location), speedDiff,
				AngleMath.rad2deg(crashAngle));
	}
}
