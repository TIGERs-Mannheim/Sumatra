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
public class BotDribbledBallTooFar extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 start;
	private final IVector2 end;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BotDribbledBallTooFar()
	{
		team = null;
		bot = 0;
		start = null;
		end = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotDribbledBallTooFar(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotDribbledBallTooFar().getByTeam());
		this.bot = event.getBotDribbledBallTooFar().getByBot();
		this.start = toVector(event.getBotDribbledBallTooFar().getStart());
		this.end = toVector(event.getBotDribbledBallTooFar().getEnd());
	}
	
	
	/**
	 * @param bot
	 * @param start
	 * @param end
	 */
	public BotDribbledBallTooFar(BotID bot, IVector2 start, IVector2 end)
	{
		super(EGameEvent.BOT_DRIBBLED_BALL_TOO_FAR);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.start = start;
		this.end = end;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.BOT_DRIBBLED_BALL_TOO_FAR);
		builder.getBotDribbledBallTooFarBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setStart(getLocationFromVector(start))
				.setEnd(getLocationFromVector(end));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s dribbled the ball %.2f mm (%s -> %s)", bot, team, start.distanceTo(end),
				formatVector(start),
				formatVector(end));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BotDribbledBallTooFar that = (BotDribbledBallTooFar) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(team, that.team)
				.append(start, that.start)
				.append(end, that.end)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(bot)
				.append(start)
				.append(end)
				.toHashCode();
	}
}
