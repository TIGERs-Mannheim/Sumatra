/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;


@Persistent
public class PlacementSucceeded extends AGameEvent
{
	private final ETeamColor team;
	private final double timeTaken;
	private final double precision;
	private final double distance;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected PlacementSucceeded()
	{
		team = null;
		timeTaken = 0;
		precision = 0;
		distance = 0;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PlacementSucceeded(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPlacementSucceeded().getByTeam());
		this.timeTaken = event.getPlacementSucceeded().getTimeTaken();
		this.precision = toDistance(event.getPlacementSucceeded().getPrecision());
		this.distance = toDistance(event.getPlacementSucceeded().getDistance());
	}
	
	
	/**
	 * @param team
	 * @param timeTaken [s]
	 * @param precision [mm]
	 * @param distance [mm]
	 */
	public PlacementSucceeded(ETeamColor team, double timeTaken, double precision, double distance)
	{
		super(EGameEvent.PLACEMENT_SUCCEEDED);
		this.team = team;
		this.timeTaken = timeTaken;
		this.precision = precision;
		this.distance = distance;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.PLACEMENT_SUCCEEDED);
		builder.getPlacementSucceededBuilder().setByTeam(getTeam(team)).setDistance((float) distance / 1000.f)
				.setPrecision((float) precision / 1000.f).setTimeTaken((float) timeTaken);
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Placement of team %s successful with a precision of %.2f mm over %.2f mm (took: %.2f s)",
				team, precision, distance, timeTaken);
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final PlacementSucceeded that = (PlacementSucceeded) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(timeTaken, that.timeTaken)
				.append(precision, that.precision)
				.append(distance, that.distance)
				.append(team, that.team)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(timeTaken)
				.append(precision)
				.append(distance)
				.toHashCode();
	}
}
