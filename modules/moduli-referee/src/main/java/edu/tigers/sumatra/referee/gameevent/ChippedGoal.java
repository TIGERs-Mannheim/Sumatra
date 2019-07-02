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
public class ChippedGoal extends AGameEvent
{
	private final ETeamColor team;
	private final Integer bot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	private final double maxBallHeight;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected ChippedGoal()
	{
		team = null;
		bot = 0;
		location = null;
		kickLocation = null;
		maxBallHeight = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public ChippedGoal(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getChippedGoal().getByTeam());
		this.bot = event.getChippedGoal().getByBot();
		this.location = toVector(event.getChippedGoal().getLocation());
		this.kickLocation = toVector(event.getChippedGoal().getKickLocation());
		this.maxBallHeight = toDistance(event.getChippedGoal().getMaxBallHeight());
	}
	
	
	/**
	 * @param bot
	 * @param location
	 * @param kickLocation
	 * @param maxBallHeight [mm]
	 */
	public ChippedGoal(BotID bot, IVector2 location, IVector2 kickLocation,
			double maxBallHeight)
	{
		super(EGameEvent.CHIP_ON_GOAL);
		this.team = bot == null ? null : bot.getTeamColor();
		this.bot = bot == null ? null : bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
		this.maxBallHeight = maxBallHeight;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.CHIPPED_GOAL);
		builder.getChippedGoalBuilder()
				.setMaxBallHeight((float) maxBallHeight / 1000.f)
				.setLocation(getLocationFromVector(location));
		
		if (bot != null)
		{
			builder.getChippedGoalBuilder().setByBot(bot);
		}
		
		if (team != null)
		{
			builder.getChippedGoalBuilder().setByTeam(getTeam(team));
		}
		
		if (kickLocation != null)
		{
			builder.getChippedGoalBuilder().setKickLocation(getLocationFromVector(kickLocation));
		}
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s chipped onto the goal (%s -> %s) with hmax=%.2f mm", bot, team,
				formatVector(kickLocation), formatVector(location), maxBallHeight);
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final ChippedGoal that = (ChippedGoal) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(maxBallHeight, that.maxBallHeight)
				.append(team, that.team)
				.append(location, that.location)
				.append(kickLocation, that.kickLocation)
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
				.append(kickLocation)
				.append(maxBallHeight)
				.toHashCode();
	}
}
