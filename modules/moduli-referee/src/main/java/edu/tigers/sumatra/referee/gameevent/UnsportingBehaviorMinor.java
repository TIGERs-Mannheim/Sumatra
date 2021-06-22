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
public class UnsportingBehaviorMinor extends AGameEvent
{
	ETeamColor team;
	String reason;


	@SuppressWarnings("unsued") // used by berkeley
	protected UnsportingBehaviorMinor()
	{
		team = null;
		reason = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public UnsportingBehaviorMinor(SslGcGameEvent.GameEvent event)
	{
		super(event);
		team = toTeamColor(event.getUnsportingBehaviorMinor().getByTeam());
		reason = event.getUnsportingBehaviorMinor().getReason();
	}


	public UnsportingBehaviorMinor(final ETeamColor team, final String reason)
	{
		super(EGameEvent.UNSPORTING_BEHAVIOR_MINOR);
		this.team = team;
		this.reason = reason;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.UNSPORTING_BEHAVIOR_MINOR);
		builder.getUnsportingBehaviorMinorBuilder()
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
		return "Minor unsporting behavior by " + team + ": " + reason;
	}
}
