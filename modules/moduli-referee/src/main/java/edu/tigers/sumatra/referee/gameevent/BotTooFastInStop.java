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
public class BotTooFastInStop extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double speed;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BotTooFastInStop()
	{
		team = null;
		bot = 0;
		location = null;
		speed = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotTooFastInStop(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotTooFastInStop().getByTeam());
		this.bot = event.getBotTooFastInStop().getByBot();
		this.location = toVector(event.getBotTooFastInStop().getLocation());
		this.speed = event.getBotTooFastInStop().getSpeed();
	}
	
	
	/**
	 * @param bot
	 * @param location
	 * @param speed [m/s]
	 */
	public BotTooFastInStop(BotID bot, IVector2 location, double speed)
	{
		super(EGameEvent.BOT_TOO_FAST_IN_STOP);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.speed = speed;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.BOT_TOO_FAST_IN_STOP);
		builder.getBotTooFastInStopBuilder().setByTeam(getTeam(team)).setByBot(bot).setSpeed((float) speed)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s moved to fast during stop: vmax=%.2f @ %s", bot, team, speed,
				formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BotTooFastInStop that = (BotTooFastInStop) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(speed, that.speed)
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
				.append(bot)
				.append(location)
				.append(speed)
				.toHashCode();
	}
}
