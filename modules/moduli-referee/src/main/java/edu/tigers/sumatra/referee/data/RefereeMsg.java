/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.data;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.RefereeProtoUtil;
import edu.tigers.sumatra.referee.gameevent.GameEventFactory;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Complete referee command
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefereeMsg
{
	/**
	 * The timestamp of the last world frame when this message was received [ns]
	 */
	long frameTimestamp;
	Command command;
	/**
	 * in microseconds
	 */
	long cmdTimestamp;
	long cmdCounter;
	/**
	 * in microseconds
	 */
	long packetTimestamp;
	SslGcRefereeMessage.Referee.Stage stage;
	/**
	 * microseconds left in the stage
	 */
	long stageTimeLeft;
	TeamInfo teamInfoYellow;
	TeamInfo teamInfoBlue;

	ETeamColor negativeHalfTeam;
	/**
	 * Designated ball position in vision coordinates
	 */
	IVector2 ballPlacementPos;

	Command nextCommand;
	List<IGameEvent> gameEvents;
	List<GameEventProposalGroup> gameEventProposalGroups;

	/**
	 * in seconds
	 */
	double currentActionTimeRemaining;


	/**
	 * Create default referee msg
	 */
	public RefereeMsg()
	{
		frameTimestamp = 0;
		command = Command.HALT;
		cmdTimestamp = 0;
		cmdCounter = -1;
		packetTimestamp = 0;
		stage = SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF;
		stageTimeLeft = 0;
		teamInfoYellow = new TeamInfo();
		teamInfoBlue = new TeamInfo();
		ballPlacementPos = null;
		negativeHalfTeam = Geometry.getNegativeHalfTeam();
		nextCommand = null;
		gameEvents = new ArrayList<>();
		gameEventProposalGroups = new ArrayList<>();
		currentActionTimeRemaining = 0;
	}


	/**
	 * Create a referee message based on a protobuf message
	 *
	 * @param frameTimestamp the Sumatra-internal timestamp
	 * @param sslRefereeMsg  the protobuf message
	 */
	public RefereeMsg(final long frameTimestamp, final SslGcRefereeMessage.Referee sslRefereeMsg)
	{
		this.frameTimestamp = frameTimestamp;
		command = sslRefereeMsg.getCommand();
		cmdTimestamp = sslRefereeMsg.getCommandTimestamp();
		cmdCounter = sslRefereeMsg.getCommandCounter();
		packetTimestamp = sslRefereeMsg.getPacketTimestamp();
		stage = sslRefereeMsg.getStage();
		stageTimeLeft = sslRefereeMsg.getStageTimeLeft();

		teamInfoYellow = new TeamInfo(sslRefereeMsg.getYellow());
		teamInfoBlue = new TeamInfo(sslRefereeMsg.getBlue());

		if (sslRefereeMsg.hasDesignatedPosition())
		{
			SslGcRefereeMessage.Referee.Point msgBallPos = sslRefereeMsg.getDesignatedPosition();
			ballPlacementPos = Vector2.fromXY(msgBallPos.getX(), msgBallPos.getY());
		} else
		{
			ballPlacementPos = null;
		}

		negativeHalfTeam = sslRefereeMsg.getBlueTeamOnPositiveHalf() ? ETeamColor.YELLOW : ETeamColor.BLUE;

		nextCommand = sslRefereeMsg.hasNextCommand() ? sslRefereeMsg.getNextCommand() : null;
		gameEvents = sslRefereeMsg.getGameEventsList().stream()
				.map(GameEventFactory::fromProtobuf)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		gameEventProposalGroups = sslRefereeMsg.getGameEventProposalsList().stream()
				.map(this::mapProposedGameEvent)
				.collect(Collectors.toList());
		currentActionTimeRemaining = sslRefereeMsg.getCurrentActionTimeRemaining() / 1e6;
	}


	/**
	 * Copy constructor
	 *
	 * @param refereeMsg the message to copy
	 */
	public RefereeMsg(final RefereeMsg refereeMsg)
	{
		frameTimestamp = refereeMsg.getFrameTimestamp();
		command = refereeMsg.command;
		cmdTimestamp = refereeMsg.cmdTimestamp;
		cmdCounter = refereeMsg.cmdCounter;
		packetTimestamp = refereeMsg.packetTimestamp;
		stage = refereeMsg.stage;
		stageTimeLeft = refereeMsg.stageTimeLeft;
		teamInfoYellow = refereeMsg.teamInfoYellow;
		teamInfoBlue = refereeMsg.teamInfoBlue;
		negativeHalfTeam = refereeMsg.negativeHalfTeam;
		ballPlacementPos = refereeMsg.getBallPlacementPosNeutral();
		nextCommand = refereeMsg.nextCommand;
		gameEvents = refereeMsg.gameEvents;
		gameEventProposalGroups = refereeMsg.gameEventProposalGroups;
		currentActionTimeRemaining = refereeMsg.currentActionTimeRemaining;
	}


	private GameEventProposalGroup mapProposedGameEvent(SslGcRefereeMessage.GameEventProposalGroup event)
	{
		List<IGameEvent> mappedGameEvents = event.getGameEventList().stream()
				.map(GameEventFactory::fromProtobuf)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		return new GameEventProposalGroup(mappedGameEvents);
	}


	/**
	 * Get the keeper id for a team
	 *
	 * @param color the team color
	 * @return the bot id
	 */
	public final BotID getKeeperBotID(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return BotID.createBotId(teamInfoBlue.getGoalie(), color);
			case YELLOW:
				return BotID.createBotId(teamInfoYellow.getGoalie(), color);
			default:
				throw new IllegalArgumentException();
		}
	}


	public ETeamColor getTeamFromCommand()
	{
		if (command != null)
		{
			return RefereeProtoUtil.teamForCommand(command);
		}
		return ETeamColor.NEUTRAL;
	}


	/**
	 * Return the {@link TeamInfo} for the specified team {@code color}
	 *
	 * @param color the team color
	 * @return {@code TeamInfo} of the specified team
	 * @throws IllegalArgumentException if {@code color} is not {@link ETeamColor#BLUE} or {@link ETeamColor#YELLOW}
	 */
	public final TeamInfo getTeamInfo(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return teamInfoBlue;
			case YELLOW:
				return teamInfoYellow;
			default:
				throw new IllegalArgumentException("Please specify a valid team color. The following value is invalid: "
						+ color);
		}

	}


	/**
	 * The position that the ball is to be placed at by the designated team
	 *
	 * @return the ballPlacementPos in vision coordinates
	 */
	public IVector2 getBallPlacementPosNeutral()
	{
		return ballPlacementPos;
	}


	/**
	 * Get the current score of both teams as map
	 *
	 * @return a map with keys value pairs for {@code BLUE} and {@code YELLOW}
	 */
	public Map<ETeamColor, Integer> getGoals()
	{
		int goalsYellow = teamInfoYellow.getScore();
		int goalsBlue = teamInfoBlue.getScore();

		return buildMap(goalsBlue, goalsYellow);
	}


	private <T> Map<ETeamColor, T> buildMap(final T blue, final T yellow)
	{
		Map<ETeamColor, T> map = new EnumMap<>(ETeamColor.class);
		map.put(ETeamColor.YELLOW, yellow);
		map.put(ETeamColor.BLUE, blue);
		return map;
	}


	public ETeamColor getTeamFromNextCommand()
	{
		if (nextCommand != null)
		{
			return RefereeProtoUtil.teamForCommand(nextCommand);
		}
		return ETeamColor.NEUTRAL;
	}
}
