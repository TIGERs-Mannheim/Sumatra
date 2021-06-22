/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.control;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcChange;
import edu.tigers.sumatra.referee.proto.SslGcCommon;
import edu.tigers.sumatra.referee.proto.SslGcGeometry;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.proto.SslGcState;

import java.util.EnumMap;
import java.util.Map;


/**
 * This factory helps in creating new game controller events that can be send to the game-controller.
 */
public final class GcEventFactory
{
	private static final Map<SslGcRefereeMessage.Referee.Command, SslGcState.Command> commandMap = new EnumMap<>(
			SslGcRefereeMessage.Referee.Command.class);

	static
	{
		commandMap.put(SslGcRefereeMessage.Referee.Command.HALT,
				SslGcState.Command.newBuilder().setType(SslGcState.Command.Type.HALT).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.STOP,
				SslGcState.Command.newBuilder().setType(SslGcState.Command.Type.STOP).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.NORMAL_START,
				SslGcState.Command.newBuilder().setType(SslGcState.Command.Type.NORMAL_START).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.FORCE_START,
				SslGcState.Command.newBuilder().setType(SslGcState.Command.Type.FORCE_START).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.PREPARE_KICKOFF_YELLOW, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.KICKOFF).setForTeam(SslGcCommon.Team.YELLOW).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.PREPARE_KICKOFF_BLUE, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.KICKOFF).setForTeam(SslGcCommon.Team.BLUE).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.PREPARE_PENALTY_YELLOW, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.PENALTY).setForTeam(SslGcCommon.Team.YELLOW).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.PREPARE_PENALTY_BLUE, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.PENALTY).setForTeam(SslGcCommon.Team.BLUE).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.DIRECT_FREE_YELLOW, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.DIRECT).setForTeam(SslGcCommon.Team.YELLOW).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.DIRECT_FREE_BLUE, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.DIRECT).setForTeam(SslGcCommon.Team.BLUE).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.INDIRECT_FREE_YELLOW, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.INDIRECT).setForTeam(SslGcCommon.Team.YELLOW).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.INDIRECT_FREE_BLUE, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.INDIRECT).setForTeam(SslGcCommon.Team.BLUE).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.TIMEOUT_YELLOW, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.TIMEOUT).setForTeam(SslGcCommon.Team.YELLOW).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.TIMEOUT_BLUE, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.TIMEOUT).setForTeam(SslGcCommon.Team.BLUE).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.BALL_PLACEMENT_YELLOW, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.BALL_PLACEMENT).setForTeam(SslGcCommon.Team.YELLOW).build());
		commandMap.put(SslGcRefereeMessage.Referee.Command.BALL_PLACEMENT_BLUE, SslGcState.Command.newBuilder()
				.setType(SslGcState.Command.Type.BALL_PLACEMENT).setForTeam(SslGcCommon.Team.BLUE).build());
	}

	private GcEventFactory()
	{
	}


	private static SslGcState.Command map(SslGcRefereeMessage.Referee.Command command)
	{
		return commandMap.get(command);
	}


	public static SslGcCommon.Team map(ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case YELLOW:
				return SslGcCommon.Team.YELLOW;
			case BLUE:
				return SslGcCommon.Team.BLUE;
			case NEUTRAL:
			default:
				return SslGcCommon.Team.UNKNOWN;
		}
	}


	public static SslGcApi.Input command(SslGcRefereeMessage.Referee.Command command)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setNewCommand(SslGcChange.NewCommand.newBuilder()
								.setCommand(map(command))
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input command(SslGcState.Command.Type type, ETeamColor teamColor)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setNewCommand(SslGcChange.NewCommand.newBuilder()
								.setCommand(SslGcState.Command.newBuilder()
										.setType(type)
										.setForTeam(map(teamColor))
										.build())
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input commandDirect(ETeamColor teamColor)
	{
		return command(SslGcState.Command.Type.DIRECT, teamColor);
	}


	public static SslGcApi.Input commandKickoff(ETeamColor teamColor)
	{
		return command(SslGcState.Command.Type.KICKOFF, teamColor);
	}


	public static SslGcApi.Input commandPenalty(ETeamColor teamColor)
	{
		return command(SslGcState.Command.Type.PENALTY, teamColor);
	}


	public static SslGcApi.Input commandTimeout(ETeamColor teamColor)
	{
		return command(SslGcState.Command.Type.TIMEOUT, teamColor);
	}


	public static SslGcApi.Input commandBallPlacement(ETeamColor teamColor)
	{
		return command(SslGcState.Command.Type.BALL_PLACEMENT, teamColor);
	}


	public static SslGcApi.Input goals(ETeamColor teamColor, int goals)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setUpdateTeamState(SslGcChange.UpdateTeamState.newBuilder()
								.setForTeam(map(teamColor))
								.setGoals(goals)
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input goalkeeper(ETeamColor teamColor, int id)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setUpdateTeamState(SslGcChange.UpdateTeamState.newBuilder()
								.setForTeam(map(teamColor))
								.setGoalkeeper(id)
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input teamName(ETeamColor teamColor, String name)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setUpdateTeamState(SslGcChange.UpdateTeamState.newBuilder()
								.setForTeam(map(teamColor))
								.setTeamName(name)
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input ballPlacement(IVector2 location)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setSetBallPlacementPos(SslGcChange.SetBallPlacementPos.newBuilder()
								.setPos(map(location))
								.build())
						.build())
				.build();
	}


	private static SslGcGeometry.Vector2 map(final IVector2 location)
	{
		return SslGcGeometry.Vector2.newBuilder().setX((float) location.x()).setY((float) location.y()).build();
	}


	public static SslGcApi.Input stage(SslGcRefereeMessage.Referee.Stage stage)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setChangeStage(SslGcChange.ChangeStage.newBuilder()
								.setNewStage(stage)
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input endGame()
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setChangeStage(SslGcChange.ChangeStage.newBuilder()
								.setNewStage(SslGcRefereeMessage.Referee.Stage.POST_GAME)
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input yellowCard(ETeamColor teamColor)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setAddYellowCard(SslGcChange.AddYellowCard.newBuilder()
								.setForTeam(map(teamColor))
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input redCard(ETeamColor teamColor)
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setAddRedCard(SslGcChange.AddRedCard.newBuilder()
								.setForTeam(map(teamColor))
								.build())
						.build())
				.build();
	}


	public static SslGcApi.Input triggerResetMatch()
	{
		return SslGcApi.Input.newBuilder()
				.setResetMatch(true)
				.build();
	}


	public static SslGcApi.Input triggerContinue()
	{
		return SslGcApi.Input.newBuilder()
				.setChange(SslGcChange.Change.newBuilder()
						.setContinue(SslGcChange.Continue.newBuilder().build())
						.build())
				.build();
	}
}
