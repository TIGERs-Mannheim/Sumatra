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
public class KeeperHeldBall extends AGameEvent
{
	private final ETeamColor team;
	private final IVector2 location;
	private final double duration;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected KeeperHeldBall()
	{
		team = null;
		location = null;
		duration = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public KeeperHeldBall(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getKeeperHeldBall().getByTeam());
		this.location = toVector(event.getKeeperHeldBall().getLocation());
		this.duration = event.getKeeperHeldBall().getDuration();
	}
	
	
	public KeeperHeldBall(ETeamColor team, IVector2 location, double duration)
	{
		super(EGameEvent.KEEPER_HELD_BALL);
		this.team = team;
		this.location = location;
		this.duration = duration;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.KEEPER_HELD_BALL);
		builder.getKeeperHeldBallBuilder().setByTeam(getTeam(team)).setDuration((float) duration)
				.setLocation(getLocationFromVector(location));
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Keeper of team %s held ball for %.2f s @ %s", team, duration, formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final KeeperHeldBall that = (KeeperHeldBall) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
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
				.append(location)
				.append(duration)
				.toHashCode();
	}
}
