/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@EqualsAndHashCode(callSuper = true)
public class BoundaryCrossing extends AGameEvent
{
	ETeamColor team;
	IVector2 location;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BoundaryCrossing(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBoundaryCrossing().getByTeam());
		this.location = toVector(event.getBoundaryCrossing().getLocation());
	}


	public BoundaryCrossing(final ETeamColor team, final IVector2 location)
	{
		super(EGameEvent.BOUNDARY_CROSSING);
		this.team = team;
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOUNDARY_CROSSING);
		builder.getBoundaryCrossingBuilder()
				.setByTeam(getTeam(team));

		if (location != null)
		{
			builder.getBoundaryCrossingBuilder().setLocation(getLocationFromVector(location));
		}

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Team %s chipped ball over the boundary at %s", team, formatVector(location));
	}
}
