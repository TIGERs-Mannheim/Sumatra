/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class KickTimeout extends AGameEvent
{
	private final ETeamColor team;
	private final IVector2 location;
	private final double time;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected KickTimeout()
	{
		team = null;
		location = null;
		time = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public KickTimeout(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getKickTimeout().getByTeam());
		this.location = toVector(event.getKickTimeout().getLocation());
		this.time = event.getKickTimeout().getTime();
	}
	
	
	/**
	 * @param team
	 * @param location
	 * @param time [s]
	 */
	public KickTimeout(ETeamColor team, final IVector2 location, double time)
	{
		super(EGameEvent.KICK_TIMEOUT);
		this.team = team;
		this.location = location;
		this.time = time;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.KICK_TIMEOUT);
		builder.getKickTimeoutBuilder()
				.setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setTime((float) time);
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Timeout for kick of team %s after %.2f s", team, time);
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final KickTimeout that = (KickTimeout) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(time, that.time)
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
				.append(location)
				.append(time)
				.toHashCode();
	}
}
