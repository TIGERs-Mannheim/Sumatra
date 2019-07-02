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
public class BotKickedBallToFast extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double initialBallSpeed;
	private final EKickType kickType;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BotKickedBallToFast()
	{
		team = null;
		bot = 0;
		location = null;
		initialBallSpeed = 0;
		kickType = EKickType.STRAIGHT;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotKickedBallToFast(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotKickedBallTooFast().getByTeam());
		this.bot = event.getBotKickedBallTooFast().getByBot();
		this.location = toVector(event.getBotKickedBallTooFast().getLocation());
		this.initialBallSpeed = event.getBotKickedBallTooFast().getInitialBallSpeed();
		this.kickType = event.getBotKickedBallTooFast().getChipped() ? EKickType.CHIPPED : EKickType.STRAIGHT;
	}
	
	
	/**
	 * @param bot
	 * @param location
	 * @param initialBallSpeed [m/s]
	 * @param kickType
	 */
	public BotKickedBallToFast(BotID bot, IVector2 location, double initialBallSpeed, EKickType kickType)
	{
		super(EGameEvent.BOT_KICKED_BALL_TOO_FAST);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.initialBallSpeed = initialBallSpeed;
		this.kickType = kickType;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.BOT_KICKED_BALL_TOO_FAST);
		builder.getBotKickedBallTooFastBuilder()
				.setByTeam(getTeam(team))
				.setByBot(bot)
				.setInitialBallSpeed((float) initialBallSpeed)
				.setChipped(kickType == EKickType.CHIPPED)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s %s ball too fast: vmax=%.2f m/s @ %s",
				bot, team, kickType == EKickType.STRAIGHT ? "kicked" : "chipped", initialBallSpeed,
				formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BotKickedBallToFast that = (BotKickedBallToFast) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(initialBallSpeed, that.initialBallSpeed)
				.append(kickType, that.kickType)
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
				.append(initialBallSpeed)
				.append(kickType)
				.toHashCode();
	}
	
	public enum EKickType
	{
		STRAIGHT,
		CHIPPED
	}
}
