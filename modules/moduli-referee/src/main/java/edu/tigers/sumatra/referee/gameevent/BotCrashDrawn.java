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
public class BotCrashDrawn extends AGameEvent
{
	int botY;
	int botB;
	IVector2 location;
	double crashSpeed;
	double speedDiff;
	double crashAngle;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotCrashDrawn(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.botY = event.getBotCrashDrawn().getBotYellow();
		this.botB = event.getBotCrashDrawn().getBotBlue();
		this.location = toVector(event.getBotCrashDrawn().getLocation());
		this.crashSpeed = event.getBotCrashDrawn().getCrashSpeed();
		this.speedDiff = event.getBotCrashDrawn().getSpeedDiff();
		this.crashAngle = event.getBotCrashDrawn().getCrashAngle();
	}


	/**
	 * @param yellow
	 * @param blue
	 * @param location
	 * @param crashSpeed [m/s]
	 * @param speedDiff [m/s]
	 * @param crashAngle [rad]
	 */
	public BotCrashDrawn(BotID yellow, BotID blue, IVector2 location, double crashSpeed, double speedDiff,
			double crashAngle)
	{
		super(EGameEvent.BOT_CRASH_DRAWN);
		if ((yellow.getTeamColor() != ETeamColor.YELLOW))
		{
			throw new AssertionError();
		}
		if ((blue.getTeamColor() != ETeamColor.BLUE))
		{
			throw new AssertionError();
		}

		this.botY = yellow.getNumber();
		this.botB = blue.getNumber();
		this.location = location;
		this.crashSpeed = crashSpeed;
		this.speedDiff = speedDiff;
		this.crashAngle = crashAngle;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_CRASH_DRAWN);
		builder.getBotCrashDrawnBuilder().setBotBlue(botB).setBotYellow(botY)
				.setCrashSpeed((float) crashSpeed).setSpeedDiff((float) speedDiff)
				.setCrashAngle((float) crashAngle).setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format(
				"Bots %d YELLOW and %d BLUE crashed into each other with %.2f m/s @ %s (Δv: %.2f m/s, angle: %.0f°)",
				botY, botB, crashSpeed, formatVector(location), speedDiff, AngleMath.rad2deg(crashAngle));
	}


	@Override
	public ETeamColor getTeam()
	{
		return ETeamColor.NEUTRAL;
	}
}
