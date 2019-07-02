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
public class BotHeldBallDeliberately extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double duration;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BotHeldBallDeliberately()
	{
		team = null;
		bot = 0;
		location = null;
		duration = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotHeldBallDeliberately(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotHeldBallDeliberately().getByTeam());
		this.bot = event.getBotHeldBallDeliberately().getByBot();
		this.location = toVector(event.getBotHeldBallDeliberately().getLocation());
		this.duration = event.getBotHeldBallDeliberately().getDuration();
	}
	
	
	/**
	 * @param bot
	 * @param location
	 * @param duration [s]
	 */
	public BotHeldBallDeliberately(BotID bot, IVector2 location, double duration)
	{
		super(EGameEvent.BOT_HELD_BALL_DELIBERATELY);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.duration = duration;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.BOT_HELD_BALL_DELIBERATELY);
		builder.getBotHeldBallDeliberatelyBuilder().setByBot(bot).setByTeam(getTeam(team)).setDuration((float) duration)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s hold the ball deliberately for %.2f s @ %s", bot, team, duration,
				formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BotHeldBallDeliberately that = (BotHeldBallDeliberately) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(duration, that.duration)
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
				.append(duration)
				.toHashCode();
	}
}
