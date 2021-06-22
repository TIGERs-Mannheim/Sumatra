/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Persistent
@Value
@EqualsAndHashCode(callSuper = true)
public class PenaltyKickFailed extends AGameEvent
{
	ETeamColor team;
	IVector2 location;

	@SuppressWarnings("unsued") // used by berkeley
	protected PenaltyKickFailed()
	{
		team = null;
		location = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PenaltyKickFailed(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPenaltyKickFailed().getByTeam());
		this.location = toVector(event.getPenaltyKickFailed().getLocation());
	}


	public PenaltyKickFailed(final ETeamColor team, final IVector2 location)
	{
		super(EGameEvent.PENALTY_KICK_FAILED);
		this.team = team;
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.PENALTY_KICK_FAILED);
		builder.getPenaltyKickFailedBuilder()
				.setByTeam(getTeam(team));

		if (location != null)
		{
			builder.getPenaltyKickFailedBuilder().setLocation(getLocationFromVector(location));
		}

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Penalty kick failed by %s at %s", team, formatVector(location));
	}
}
