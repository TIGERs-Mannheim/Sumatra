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
public class PossibleGoal extends AGameEvent
{
	ETeamColor team;
	ETeamColor kickingTeam;
	Integer kickingBot;
	IVector2 location;
	IVector2 kickLocation;
	Float maxBallHeight;
	Integer numRobotsByTeam;
	Long lastTouchedByTeam;
	String message;


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PossibleGoal(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPossibleGoal().getByTeam());
		this.kickingTeam = toTeamColor(event.getPossibleGoal().getKickingTeam());
		this.kickingBot = event.getPossibleGoal().getKickingBot();
		this.location = toVector(event.getPossibleGoal().getLocation());
		this.kickLocation = toVector(event.getPossibleGoal().getKickLocation());
		this.maxBallHeight = event.getPossibleGoal().getMaxBallHeight() * 1000;
		this.numRobotsByTeam = event.getPossibleGoal().getNumRobotsByTeam();
		this.lastTouchedByTeam = event.getPossibleGoal().getLastTouchByTeam() * 1000;
		this.message = event.getPossibleGoal().getMessage();
	}


	public PossibleGoal(ETeamColor forTeam, BotID bot, IVector2 location, IVector2 kickLocation,
			double maxBallHeight, int numRobotsByTeam, long lastTouchedByTeam)
	{
		super(EGameEvent.POSSIBLE_GOAL);
		this.team = forTeam;
		this.kickingTeam = bot == null ? null : bot.getTeamColor();
		this.kickingBot = bot == null ? null : bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
		this.maxBallHeight = (float) maxBallHeight;
		this.numRobotsByTeam = numRobotsByTeam;
		this.lastTouchedByTeam = lastTouchedByTeam;
		this.message = "";
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.POSSIBLE_GOAL);
		builder.getPossibleGoalBuilder()
				.setByTeam(getTeam(team))
				.setMaxBallHeight(maxBallHeight / 1000.0f)
				.setNumRobotsByTeam(numRobotsByTeam)
				.setLastTouchByTeam(lastTouchedByTeam / 1000);

		if (kickingTeam != null)
		{
			builder.getPossibleGoalBuilder().setKickingTeam(getTeam(kickingTeam));
		}
		if (kickingBot != null)
		{
			builder.getPossibleGoalBuilder().setKickingBot(kickingBot);
		}
		if (location != null)
		{
			builder.getPossibleGoalBuilder().setLocation(getLocationFromVector(location));
		}
		if (kickLocation != null)
		{
			builder.getPossibleGoalBuilder().setKickLocation(getLocationFromVector(kickLocation));
		}
		if (message != null)
		{
			builder.getPossibleGoalBuilder().setMessage(message);
		}
		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s scored possible goal for %s (%s -> %s)",
				kickingBot, kickingTeam, team,
				formatVector(kickLocation), formatVector(location));
	}
}
