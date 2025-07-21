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
public class BotSubstitution extends AGameEvent
{
	ETeamColor team;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotSubstitution(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotSubstitution().getByTeam());
	}


	public BotSubstitution(ETeamColor team)
	{
		super(EGameEvent.BOT_SUBSTITUTION);
		this.team = team;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_SUBSTITUTION);
		builder.getBotSubstitutionBuilder().setByTeam(getTeam(team));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Team %s wants to substitute a bot", team);
	}
}
