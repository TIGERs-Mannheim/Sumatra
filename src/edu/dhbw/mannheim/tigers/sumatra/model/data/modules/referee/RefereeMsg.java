/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s):
 * Bernhard
 * Gunther
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;


/**
 * Complete referee command
 */
@Embeddable
public class RefereeMsg
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private SSL_Referee		sslRefereeMsg;
	private TeamProps			teamProps;
	
	private ETeamSpecRefCmd	teamSpecRefCmd;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param sslRefereeMsg
	 * @param teamProps
	 */
	public RefereeMsg(SSL_Referee sslRefereeMsg, TeamProps teamProps)
	{
		this.sslRefereeMsg = sslRefereeMsg;
		this.teamProps = teamProps;
		teamSpecRefCmd = createTeamSpecRefCmd(sslRefereeMsg, teamProps);
	}
	
	
	private ETeamSpecRefCmd createTeamSpecRefCmd(SSL_Referee sslRefereeMsg, TeamProps teamProps)
	{
		switch (sslRefereeMsg.getCommand())
		{
			case DIRECT_FREE_BLUE:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.DirectFreeKickEnemies
						: ETeamSpecRefCmd.DirectFreeKickTigers;
			case DIRECT_FREE_YELLOW:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.DirectFreeKickTigers
						: ETeamSpecRefCmd.DirectFreeKickEnemies;
			case INDIRECT_FREE_BLUE:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.IndirectFreeKickEnemies
						: ETeamSpecRefCmd.IndirectFreeKickTigers;
			case INDIRECT_FREE_YELLOW:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.IndirectFreeKickTigers
						: ETeamSpecRefCmd.IndirectFreeKickEnemies;
			case PREPARE_KICKOFF_BLUE:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.KickOffEnemies : ETeamSpecRefCmd.KickOffTigers;
			case PREPARE_KICKOFF_YELLOW:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.KickOffTigers : ETeamSpecRefCmd.KickOffEnemies;
			case PREPARE_PENALTY_BLUE:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.PenaltyEnemies : ETeamSpecRefCmd.PenaltyTigers;
			case PREPARE_PENALTY_YELLOW:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.PenaltyTigers : ETeamSpecRefCmd.PenaltyEnemies;
			case TIMEOUT_BLUE:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.TimeoutEnemies : ETeamSpecRefCmd.TimeoutTigers;
			case TIMEOUT_YELLOW:
				return teamProps.getTigersAreYellow() ? ETeamSpecRefCmd.TimeoutTigers : ETeamSpecRefCmd.TimeoutEnemies;
			default:
				return ETeamSpecRefCmd.NoCommand;
		}
	}
	
	
	/**
	 * @return the sslRefereeMsg
	 */
	public final SSL_Referee getSslRefereeMsg()
	{
		return sslRefereeMsg;
	}
	
	
	/**
	 * @return
	 */
	public final Command getCommand()
	{
		return sslRefereeMsg.getCommand();
	}
	
	
	/**
	 * @return
	 */
	public final long getCommandTimestamp()
	{
		return sslRefereeMsg.getCommandTimestamp();
	}
	
	
	/**
	 * @return
	 */
	public final long getCommandCounter()
	{
		return sslRefereeMsg.getCommandCounter();
	}
	
	
	/**
	 * @return
	 */
	public final long getPacketTimestamp()
	{
		return sslRefereeMsg.getPacketTimestamp();
	}
	
	
	/**
	 * @return
	 */
	public final Stage getStage()
	{
		return sslRefereeMsg.getStage();
	}
	
	
	/**
	 * @return
	 */
	public final long getStageTimeLeft()
	{
		return sslRefereeMsg.getStageTimeLeft();
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoBlue()
	{
		return sslRefereeMsg.getBlue();
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoYellow()
	{
		return sslRefereeMsg.getYellow();
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoTigers()
	{
		if (teamProps.getTigersAreYellow())
		{
			return sslRefereeMsg.getYellow();
		}
		return sslRefereeMsg.getBlue();
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoThem()
	{
		if (teamProps.getTigersAreYellow())
		{
			return sslRefereeMsg.getBlue();
		}
		return sslRefereeMsg.getYellow();
	}
	
	
	/**
	 * @return the teamProps
	 */
	public final TeamProps getTeamProps()
	{
		return teamProps;
	}
	
	
	/**
	 * @return the teamSpecRefCmd
	 */
	public final ETeamSpecRefCmd getTeamSpecRefCmd()
	{
		return teamSpecRefCmd;
	}
}
