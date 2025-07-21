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
public class NoProgressInGame extends AGameEvent
{
	double time;
	IVector2 location;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public NoProgressInGame(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.time = event.getNoProgressInGame().getTime();
		this.location = toVector(event.getNoProgressInGame().getLocation());
	}


	/**
	 * @param pos
	 * @param time [s]
	 */
	public NoProgressInGame(IVector2 pos, double time)
	{
		super(EGameEvent.NO_PROGRESS_IN_GAME);
		this.location = pos;
		this.time = time;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.NO_PROGRESS_IN_GAME);
		builder.getNoProgressInGameBuilder().setTime((float) time).setLocation(getLocationFromVector(location));
		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("No progress in Game for %.2f s @ %s", time, formatVector(location));
	}


	@Override
	public ETeamColor getTeam()
	{
		return ETeamColor.NEUTRAL;
	}
}
