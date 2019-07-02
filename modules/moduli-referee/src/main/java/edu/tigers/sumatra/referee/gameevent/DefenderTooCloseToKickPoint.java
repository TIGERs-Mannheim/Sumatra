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
public class DefenderTooCloseToKickPoint extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double distance;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected DefenderTooCloseToKickPoint()
	{
		team = null;
		bot = 0;
		location = null;
		distance = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public DefenderTooCloseToKickPoint(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getDefenderTooCloseToKickPoint().getByTeam());
		this.bot = event.getDefenderTooCloseToKickPoint().getByBot();
		this.location = toVector(event.getDefenderTooCloseToKickPoint().getLocation());
		this.distance = toDistance(event.getDefenderTooCloseToKickPoint().getDistance());
	}
	
	
	/**
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public DefenderTooCloseToKickPoint(BotID bot, IVector2 location, double distance)
	{
		super(EGameEvent.DEFENDER_TOO_CLOSE_TO_KICK_POINT);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.DEFENDER_TOO_CLOSE_TO_KICK_POINT);
		builder.getDefenderTooCloseToKickPointBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Defender %d %s was too close to the kick point: %.2f mm @ %s", bot, team, distance,
				formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final DefenderTooCloseToKickPoint that = (DefenderTooCloseToKickPoint) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(distance, that.distance)
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
				.append(distance)
				.toHashCode();
	}
}
