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
public class BotCrashDrawn extends AGameEvent
{
	private final int botY;
	private final int botB;
	private final IVector2 location;
	private final double crashSpeed;
	private final double speedDiff;
	private final double crashAngle;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BotCrashDrawn()
	{
		botY = 0;
		botB = 0;
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
	public BotCrashDrawn(SslGameEvent.GameEvent event)
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
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.BOT_CRASH_DRAWN);
		builder.getBotCrashDrawnBuilder().setBotBlue(botB).setBotYellow(botY)
				.setCrashSpeed((float) crashSpeed).setSpeedDiff((float) speedDiff)
				.setCrashAngle((float) crashAngle).setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format(
				"Bots %d YELLOW and %d BLUE crashed into each other with %.2f m/s @ %s (Δv: %.2f m/s, angle: %.0f°)",
				botY, botB, crashSpeed, formatVector(location), speedDiff, AngleMath.rad2deg(crashAngle));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BotCrashDrawn that = (BotCrashDrawn) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(botY, that.botY)
				.append(botB, that.botB)
				.append(crashSpeed, that.crashSpeed)
				.append(speedDiff, that.speedDiff)
				.append(crashAngle, that.crashAngle)
				.append(location, that.location)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(botY)
				.append(botB)
				.append(location)
				.append(crashSpeed)
				.append(speedDiff)
				.append(crashAngle)
				.toHashCode();
	}
}
