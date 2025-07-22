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
public class BallLeftFieldGoalLine extends AGameEvent
{
	ETeamColor team;
	Integer bot;
	IVector2 location;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BallLeftFieldGoalLine(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBallLeftFieldGoalLine().getByTeam());
		this.bot = event.getBallLeftFieldGoalLine().getByBot();
		this.location = toVector(event.getBallLeftFieldGoalLine().getLocation());
	}


	public BallLeftFieldGoalLine(BotID bot, IVector2 location)
	{
		super(EGameEvent.BALL_LEFT_FIELD_GOAL_LINE);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}


	public BallLeftFieldGoalLine(ETeamColor team, IVector2 location)
	{
		super(EGameEvent.BALL_LEFT_FIELD_GOAL_LINE);
		this.team = team;
		this.bot = null;
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();

		builder.setType(SslGcGameEvent.GameEvent.Type.BALL_LEFT_FIELD_GOAL_LINE);
		builder.getBallLeftFieldGoalLineBuilder()
				.setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));

		if (bot != null)
		{
			builder.getBallLeftFieldGoalLineBuilder().setByBot(bot);
		}

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Ball left field @ %s by bot %d %s via goal line", formatVector(location), bot, team);
	}
}
