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
public class KeeperHeldBall extends AGameEvent
{
	ETeamColor team;
	IVector2 location;
	double duration;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public KeeperHeldBall(SslGcGameEvent.GameEvent event)
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
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.KEEPER_HELD_BALL);
		builder.getKeeperHeldBallBuilder().setByTeam(getTeam(team)).setDuration((float) duration)
				.setLocation(getLocationFromVector(location));
		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Keeper of team %s held ball for %.2f s @ %s", team, duration, formatVector(location));
	}
}
