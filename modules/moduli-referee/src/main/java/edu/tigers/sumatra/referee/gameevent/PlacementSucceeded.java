/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Persistent
@Value
@EqualsAndHashCode(callSuper = true)
public class PlacementSucceeded extends AGameEvent
{
	ETeamColor team;
	double timeTaken;
	double precision;
	double distance;


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
	public PlacementSucceeded(SslGcGameEvent.GameEvent event)
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
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.PLACEMENT_SUCCEEDED);
		builder.getPlacementSucceededBuilder().setByTeam(getTeam(team)).setDistance((float) distance / 1000.f)
				.setPrecision((float) precision / 1000.f).setTimeTaken((float) timeTaken);

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Placement of team %s successful with a precision of %.2f mm over %.2f mm (took: %.2f s)",
				team, precision, distance, timeTaken);
	}
}
