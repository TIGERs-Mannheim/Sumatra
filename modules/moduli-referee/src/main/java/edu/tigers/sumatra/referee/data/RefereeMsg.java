/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.data;

import java.util.EnumMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Complete referee command
 */
@Persistent(version = 1)
public class RefereeMsg
{
	/** in nanoseconds */
	private final long frameTimestamp;
	private final Command command;
	/** in microseconds */
	private final long cmdTimestamp;
	private final long cmdCounter;
	/** in microseconds */
	private final long packetTimestamp;
	private final Stage stage;
	/** microseconds left in the stage */
	private final long stageTimeLeft;
	private final TeamInfo teamInfoYellow;
	private final TeamInfo teamInfoBlue;
	
	private final ETeamColor negativeHalfTeam;
	private final IVector2 ballPlacementPos;
	
	private final GameEvent gameEvent;
	
	
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
		stage = Stage.NORMAL_FIRST_HALF;
		stageTimeLeft = 0;
		teamInfoYellow = new TeamInfo();
		teamInfoBlue = new TeamInfo();
		ballPlacementPos = Vector2f.ZERO_VECTOR;
		negativeHalfTeam = Geometry.getNegativeHalfTeam();
		gameEvent = new GameEvent();
	}
	
	
	/**
	 * Create a referee message based on a protobuf message
	 * 
	 * @param frameTimestamp the Sumatra-internal timestamp
	 * @param sslRefereeMsg the protobuf message
	 */
	public RefereeMsg(final long frameTimestamp, final SSL_Referee sslRefereeMsg)
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
			SSL_Referee.Point msgBallPos = sslRefereeMsg.getDesignatedPosition();
			ballPlacementPos = Vector2.fromXY(msgBallPos.getX(), msgBallPos.getY());
		} else
		{
			ballPlacementPos = Vector2f.ZERO_VECTOR;
		}
		
		negativeHalfTeam = Geometry.getNegativeHalfTeam();
		gameEvent = new GameEvent(sslRefereeMsg.getGameEvent());
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
		gameEvent = refereeMsg.gameEvent;
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
	
	
	/**
	 * @return
	 */
	public final Command getCommand()
	{
		return command;
	}
	
	
	/**
	 * The command timestamp value from the protobuf message
	 * 
	 * @return UNIX timestamp in microseconds
	 */
	public final long getCommandTimestamp()
	{
		return cmdTimestamp;
	}
	
	
	/**
	 * @return
	 */
	public final long getCommandCounter()
	{
		return cmdCounter;
	}
	
	
	/**
	 * The packet timestamp value from the protobuf message
	 * 
	 * @return UNIX timestamp in microseconds
	 */
	public final long getPacketTimestamp()
	{
		return packetTimestamp;
	}
	
	
	/**
	 * @return
	 */
	public final Stage getStage()
	{
		return stage;
	}
	
	
	/**
	 * The number of microseconds left in the current stage ({@link #getStage()}
	 * 
	 * @return microseconds left in the stage
	 */
	public final long getStageTimeLeft()
	{
		return stageTimeLeft;
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoBlue()
	{
		return teamInfoBlue;
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoYellow()
	{
		return teamInfoYellow;
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
	 * @return the negativeHalfTeam
	 */
	public final ETeamColor getNegativeHalfTeam()
	{
		return negativeHalfTeam;
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
	 * The timestamp of the last world frame when this message was received
	 * 
	 * @return the frameTimestamp in nanoseconds
	 */
	public final long getFrameTimestamp()
	{
		return frameTimestamp;
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
	
	
	/**
	 * Return a map with the names of each team
	 * 
	 * @return
	 */
	public Map<ETeamColor, String> getTeamNames()
	{
		String yellowName = teamInfoYellow.getName();
		String blueName = teamInfoBlue.getName();
		return buildMap(blueName, yellowName);
	}
	
	
	public GameEvent getGameEvent()
	{
		return gameEvent;
	}
	
	
	private <T> Map<ETeamColor, T> buildMap(final T blue, final T yellow)
	{
		Map<ETeamColor, T> map = new EnumMap<>(ETeamColor.class);
		map.put(ETeamColor.YELLOW, yellow);
		map.put(ETeamColor.BLUE, blue);
		return map;
	}
	
	
	@Override
	public String toString()
	{
		return "RefereeMsg{" +
				"frameTimestamp=" + frameTimestamp +
				", command=" + command +
				", cmdTimestamp=" + cmdTimestamp +
				", cmdCounter=" + cmdCounter +
				", packetTimestamp=" + packetTimestamp +
				", stage=" + stage +
				", stageTimeLeft=" + stageTimeLeft +
				", teamInfoYellow=" + teamInfoYellow +
				", teamInfoBlue=" + teamInfoBlue +
				", negativeHalfTeam=" + negativeHalfTeam +
				", ballPlacementPos=" + ballPlacementPos +
				'}';
	}
}
