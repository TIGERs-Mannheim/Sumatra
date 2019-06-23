/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s):
 * Bernhard
 * Gunther
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;


/**
 * Complete referee command
 */
@Persistent
public class RefereeMsg
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final ETeamColor		color;
	private final ETeamSpecRefCmd	teamSpecRefCmd;
	
	private final Command			command;
	private final long				cmdTimestamp;
	private final long				cmdCounter;
	private final long				packetTimestamp;
	private final Stage				stage;
	private final long				stageTimeLeft;
	private final TeamInfo			teamInfoYellow;
	private final TeamInfo			teamInfoBlue;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private RefereeMsg()
	{
		color = ETeamColor.UNINITIALIZED;
		teamSpecRefCmd = ETeamSpecRefCmd.NoCommand;
		command = null;
		cmdTimestamp = 0;
		cmdCounter = 0;
		packetTimestamp = 0;
		stage = Stage.NORMAL_FIRST_HALF;
		stageTimeLeft = 0;
		teamInfoYellow = null;
		teamInfoBlue = null;
	}
	
	
	/**
	 * @param sslRefereeMsg
	 * @param color
	 */
	public RefereeMsg(final SSL_Referee sslRefereeMsg, final ETeamColor color)
	{
		command = sslRefereeMsg.getCommand();
		cmdTimestamp = sslRefereeMsg.getCommandTimestamp();
		cmdCounter = sslRefereeMsg.getCommandCounter();
		packetTimestamp = sslRefereeMsg.getPacketTimestamp();
		stage = sslRefereeMsg.getStage();
		stageTimeLeft = sslRefereeMsg.getStageTimeLeft();
		
		teamInfoYellow = new TeamInfo(sslRefereeMsg.getYellow());
		teamInfoBlue = new TeamInfo(sslRefereeMsg.getBlue());
		
		teamSpecRefCmd = createTeamSpecRefCmd(sslRefereeMsg, color);
		this.color = color;
	}
	
	
	private ETeamSpecRefCmd createTeamSpecRefCmd(final SSL_Referee sslRefereeMsg, final ETeamColor color)
	{
		switch (sslRefereeMsg.getCommand())
		{
			case DIRECT_FREE_BLUE:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.DirectFreeKickEnemies
						: ETeamSpecRefCmd.DirectFreeKickTigers;
			case DIRECT_FREE_YELLOW:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.DirectFreeKickTigers
						: ETeamSpecRefCmd.DirectFreeKickEnemies;
			case INDIRECT_FREE_BLUE:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.IndirectFreeKickEnemies
						: ETeamSpecRefCmd.IndirectFreeKickTigers;
			case INDIRECT_FREE_YELLOW:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.IndirectFreeKickTigers
						: ETeamSpecRefCmd.IndirectFreeKickEnemies;
			case PREPARE_KICKOFF_BLUE:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.KickOffEnemies : ETeamSpecRefCmd.KickOffTigers;
			case PREPARE_KICKOFF_YELLOW:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.KickOffTigers : ETeamSpecRefCmd.KickOffEnemies;
			case PREPARE_PENALTY_BLUE:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.PenaltyEnemies : ETeamSpecRefCmd.PenaltyTigers;
			case PREPARE_PENALTY_YELLOW:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.PenaltyTigers : ETeamSpecRefCmd.PenaltyEnemies;
			case TIMEOUT_BLUE:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.TimeoutEnemies : ETeamSpecRefCmd.TimeoutTigers;
			case TIMEOUT_YELLOW:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.TimeoutTigers : ETeamSpecRefCmd.TimeoutEnemies;
			case GOAL_BLUE:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.GoalEnemies : ETeamSpecRefCmd.GoalTigers;
			case GOAL_YELLOW:
				return color == ETeamColor.YELLOW ? ETeamSpecRefCmd.GoalTigers : ETeamSpecRefCmd.GoalEnemies;
			case FORCE_START:
				return ETeamSpecRefCmd.ForceStart;
			case HALT:
				return ETeamSpecRefCmd.Halt;
			case NORMAL_START:
				return ETeamSpecRefCmd.NormalStart;
			case STOP:
				return ETeamSpecRefCmd.Stop;
		}
		return ETeamSpecRefCmd.NoCommand;
	}
	
	
	/**
	 * @return
	 */
	public final Command getCommand()
	{
		return command;
	}
	
	
	/**
	 * @return
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
	 * @return
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
	 * @return
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
	 * @return
	 */
	public final TeamInfo getTeamInfoTigers()
	{
		if (color == ETeamColor.YELLOW)
		{
			return teamInfoYellow;
		}
		return teamInfoBlue;
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoThem()
	{
		if (color == ETeamColor.YELLOW)
		{
			return teamInfoBlue;
		}
		return teamInfoYellow;
	}
	
	
	/**
	 * @return the teamSpecRefCmd
	 */
	public final ETeamSpecRefCmd getTeamSpecRefCmd()
	{
		return teamSpecRefCmd;
	}
	
	
	/**
	 * @return the color
	 */
	public final ETeamColor getColor()
	{
		return color;
	}
}
