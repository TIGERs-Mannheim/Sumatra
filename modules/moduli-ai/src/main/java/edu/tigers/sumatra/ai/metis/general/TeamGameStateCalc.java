/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 23, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.ETeamSpecRefCmd;
import edu.tigers.sumatra.referee.RefereeMsgTeamSpec;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Check in which game state we are by consulting new referee messages
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TeamGameStateCalc extends ACalculator
{
	private static final double	BALL_MOVED_DISTANCE_TOL	= 50;
	private IVector2					ballPosOnPrepare			= null;
	private boolean					goalScoredState			= false;
	private RefereeMsgTeamSpec		lastRefereeMsg				= null;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final EGameStateTeam lastGameState = baseAiFrame.getPrevFrame().getTacticalField().getGameState();
		RefereeMsgTeamSpec refereeMsg = baseAiFrame.getRefereeMsg();
		EGameStateTeam newGameState = lastGameState;
		
		if (lastRefereeMsg == null)
		{
			lastRefereeMsg = refereeMsg;
		}
		
		if (baseAiFrame.isNewRefereeMsg() || (lastGameState == EGameStateTeam.UNKNOWN))
		{
			switch (refereeMsg.getStage())
			{
				case NORMAL_FIRST_HALF:
				case NORMAL_SECOND_HALF:
				case EXTRA_FIRST_HALF:
				case EXTRA_SECOND_HALF:
					break;
				case NORMAL_FIRST_HALF_PRE:
				case NORMAL_SECOND_HALF_PRE:
				case EXTRA_FIRST_HALF_PRE:
				case EXTRA_SECOND_HALF_PRE:
					break;
				case NORMAL_HALF_TIME:
				case EXTRA_HALF_TIME:
				case EXTRA_TIME_BREAK:
				case PENALTY_SHOOTOUT_BREAK:
					newGameState = EGameStateTeam.BREAK;
					break;
				case POST_GAME:
					newGameState = EGameStateTeam.POST_GAME;
					break;
				case PENALTY_SHOOTOUT:
					break;
				default:
					break;
			}
			switch (refereeMsg.getTeamSpecRefCmd())
			{
				case DirectFreeKickEnemies:
					if (Geometry.getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos()))
					{
						newGameState = EGameStateTeam.DIRECT_KICK_THEY;
					} else if (baseAiFrame.getWorldFrame().getBall().getPos().x() < 0)
					{
						// ball is in our half, freekick is awarded to enemies => corner kick
						newGameState = EGameStateTeam.CORNER_KICK_THEY;
					} else
					{
						// ball is in their half, freekick is awarded to enemies => goal kick
						newGameState = EGameStateTeam.GOAL_KICK_THEY;
					}
					break;
				case DirectFreeKickTigers:
					if (Geometry.getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos()))
					{
						newGameState = EGameStateTeam.DIRECT_KICK_WE;
					} else if (baseAiFrame.getWorldFrame().getBall().getPos().x() < 0)
					{
						// ball is in our half, freekick is awarded to us => goal kick
						newGameState = EGameStateTeam.GOAL_KICK_WE;
					} else
					{
						// ball is in their half, freekick is awarded to us => corner kick
						newGameState = EGameStateTeam.CORNER_KICK_WE;
					}
					break;
				case ForceStart:
					newGameState = EGameStateTeam.RUNNING;
					goalScoredState = false;
					break;
				case GoalEnemies:
				case GoalTigers:
					goalScoredCheck(lastRefereeMsg, refereeMsg);
					break;
				case Halt:
					newGameState = EGameStateTeam.HALTED;
					break;
				case IndirectFreeKickEnemies:
					newGameState = EGameStateTeam.THROW_IN_THEY;
					break;
				case IndirectFreeKickTigers:
					newGameState = EGameStateTeam.THROW_IN_WE;
					break;
				case KickOffEnemies:
					newGameState = EGameStateTeam.PREPARE_KICKOFF_THEY;
					break;
				case KickOffTigers:
					newGameState = EGameStateTeam.PREPARE_KICKOFF_WE;
					break;
				case NoCommand:
					break;
				case NormalStart:
					if ((lastGameState == EGameStateTeam.STOPPED) || (lastGameState == EGameStateTeam.UNKNOWN))
					{
						// NORMAL_START is not allowed after STOP, but assistant ref may pressed wrong button...
						newGameState = EGameStateTeam.RUNNING;
					}
					goalScoredState = false;
					break;
				case PenaltyEnemies:
					newGameState = EGameStateTeam.PREPARE_PENALTY_THEY;
					break;
				case PenaltyTigers:
					newGameState = EGameStateTeam.PREPARE_PENALTY_WE;
					break;
				case Stop:
					newGameState = EGameStateTeam.STOPPED;
					break;
				case TimeoutEnemies:
					newGameState = EGameStateTeam.TIMEOUT_THEY;
					break;
				case TimeoutTigers:
					newGameState = EGameStateTeam.TIMEOUT_WE;
					break;
				case BallPlacementEnemies:
					newGameState = EGameStateTeam.BALL_PLACEMENT_THEY;
					break;
				case BallPlacementTigers:
					newGameState = EGameStateTeam.BALL_PLACEMENT_WE;
					break;
				default:
					throw new IllegalStateException();
			}
			
			// remember ball position on certain situations
			switch (refereeMsg.getTeamSpecRefCmd())
			{
				case DirectFreeKickEnemies:
				case DirectFreeKickTigers:
				case IndirectFreeKickEnemies:
				case IndirectFreeKickTigers:
				case NormalStart:
					ballPosOnPrepare = baseAiFrame.getWorldFrame().getBall().getPos();
					break;
				default:
					ballPosOnPrepare = null;
			}
		} else
		{
			switch (lastGameState)
			{
				case PREPARE_KICKOFF_THEY:
				case PREPARE_KICKOFF_WE:
				case PREPARE_PENALTY_THEY:
				case PREPARE_PENALTY_WE:
					if ((baseAiFrame.getRefereeMsg() != null)
							&& (baseAiFrame.getRefereeMsg().getTeamSpecRefCmd() != ETeamSpecRefCmd.NormalStart))
					{
						break;
					}
				case CORNER_KICK_THEY:
				case CORNER_KICK_WE:
				case GOAL_KICK_THEY:
				case GOAL_KICK_WE:
				case THROW_IN_THEY:
				case THROW_IN_WE:
				case DIRECT_KICK_THEY:
				case DIRECT_KICK_WE:
					if ((ballPosOnPrepare != null)
							&& (GeoMath.distancePP(baseAiFrame.getWorldFrame().getBall().getPos(),
									ballPosOnPrepare) > BALL_MOVED_DISTANCE_TOL))
					{
						ballPosOnPrepare = null;
						newGameState = EGameStateTeam.RUNNING;
						break;
					}
				case HALTED:
					break;
				case RUNNING:
					goalScoredState = false;
					break;
				case STOPPED:
					break;
				case TIMEOUT_THEY:
					break;
				case TIMEOUT_WE:
					break;
				case UNKNOWN:
					break;
				case BREAK:
					break;
				case POST_GAME:
					break;
				case BALL_PLACEMENT_THEY:
					break;
				case BALL_PLACEMENT_WE:
					break;
				default:
					throw new IllegalStateException();
			}
		}
		
		newTacticalField.setGameState(newGameState);
		newTacticalField.setGoalScored(goalScoredState);
		lastRefereeMsg = refereeMsg;
	}
	
	
	private void goalScoredCheck(final RefereeMsgTeamSpec latestRef, final RefereeMsgTeamSpec currentRef)
	{
		if ((latestRef != null) && (currentRef != null))
		{
			int scoreTigersCurrent = currentRef.getTeamInfoTigers().getScore();
			int scoreTigersLast = latestRef.getTeamInfoTigers().getScore();
			int scoreFoesCurrent = currentRef.getTeamInfoThem().getScore();
			int scoreFoesLast = latestRef.getTeamInfoThem().getScore();
			if ((scoreTigersCurrent > scoreTigersLast) || (scoreFoesCurrent > scoreFoesLast))
			{
				goalScoredState = true;
			}
		}
		
	}
}
