/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@EqualsAndHashCode(callSuper = true)
public class PlacementFailed extends AGameEvent
{
	ETeamColor team;
	double remainingDistance;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PlacementFailed(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPlacementFailed().getByTeam());
		this.remainingDistance = toDistance(event.getPlacementFailed().getRemainingDistance());
	}


	public PlacementFailed(final ETeamColor team, final double remainingDistance)
	{
		super(EGameEvent.PLACEMENT_FAILED);
		this.team = team;
		this.remainingDistance = remainingDistance;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.PLACEMENT_FAILED);
		builder.getPlacementFailedBuilder()
				.setByTeam(getTeam(team))
				.setRemainingDistance((float) remainingDistance / 1000f)
				.build();

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Placement failed by %s: %.2f mm remaining", team, remainingDistance);
	}
}
