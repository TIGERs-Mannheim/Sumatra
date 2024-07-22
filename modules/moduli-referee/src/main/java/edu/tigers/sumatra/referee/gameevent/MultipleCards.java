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
public class MultipleCards extends AGameEvent
{
	ETeamColor team;


	@SuppressWarnings("unsued") // used by berkeley
	protected MultipleCards()
	{
		team = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public MultipleCards(SslGcGameEvent.GameEvent event)
	{
		super(event);
		team = toTeamColor(event.getMultipleCards().getByTeam());
	}


	public MultipleCards(final ETeamColor team)
	{
		super(EGameEvent.MULTIPLE_CARDS);
		this.team = team;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.MULTIPLE_CARDS);
		builder.getMultipleCardsBuilder()
				.setByTeam(getTeam(team));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return "Multiple cards collected by " + team;
	}
}
