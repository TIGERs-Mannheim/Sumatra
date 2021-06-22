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
public class UnsportingBehaviorMajor extends AGameEvent
{
	ETeamColor team;
	String reason;


	@SuppressWarnings("unsued") // used by berkeley
	protected UnsportingBehaviorMajor()
	{
		reason = null;
		team = null;
	}

	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public UnsportingBehaviorMajor(SslGcGameEvent.GameEvent event)
	{
		super(event);
		team = toTeamColor(event.getUnsportingBehaviorMajor().getByTeam());
		reason = event.getUnsportingBehaviorMajor().getReason();
	}


	public UnsportingBehaviorMajor(final ETeamColor team, final String reason)
	{
		super(EGameEvent.UNSPORTING_BEHAVIOR_MAJOR);
		this.team = team;
		this.reason = reason;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.UNSPORTING_BEHAVIOR_MAJOR);
		builder.getUnsportingBehaviorMajorBuilder()
				.setByTeam(getTeam(team))
				.setReason(reason);

		return builder.build();
	}


	public ETeamColor getTeam()
	{
		return team;
	}


	public String getReason()
	{
		return reason;
	}


	@Override
	public String getDescription()
	{
		return "Major unsporting behavior by " + team + ": " + reason;
	}
}
