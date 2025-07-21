/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@EqualsAndHashCode(callSuper = true)
public class AttackerDoubleTouchedBall extends AGameEvent
{
	ETeamColor team;
	int bot;
	IVector2 location;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AttackerDoubleTouchedBall(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAttackerDoubleTouchedBall().getByTeam());
		this.bot = event.getAttackerDoubleTouchedBall().getByBot();
		this.location = toVector(event.getAttackerDoubleTouchedBall().getLocation());
	}


	public AttackerDoubleTouchedBall(BotID bot, IVector2 location)
	{
		super(EGameEvent.ATTACKER_DOUBLE_TOUCHED_BALL);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.ATTACKER_DOUBLE_TOUCHED_BALL);
		builder.getAttackerDoubleTouchedBallBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Attacker %d %s double touched ball @ %s", bot, team, formatVector(location));
	}
}
