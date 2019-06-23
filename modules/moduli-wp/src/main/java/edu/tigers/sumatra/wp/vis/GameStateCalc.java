/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 23, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Check in which game state we are by consulting new referee messages
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GameStateCalc implements IWpCalc
{
	private static final double	BALL_MOVED_DISTANCE_TOL	= 50;
	private IVector2					ballPosOnPrepare			= null;
	private long						lastRefMsgCounter			= -1;
	private EGameStateNeutral		lastGameState				= EGameStateNeutral.UNKNOWN;
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		RefereeMsg refereeMsg = wfw.getRefereeMsg();
		EGameStateNeutral newGameState = lastGameState;
		IVector2 ballPos = wfw.getSimpleWorldFrame().getBall().getPos();
		
		if ((refereeMsg.getCommandCounter() != lastRefMsgCounter) || (lastGameState == EGameStateNeutral.UNKNOWN))
		{
			lastRefMsgCounter = refereeMsg.getCommandCounter();
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
					newGameState = EGameStateNeutral.BREAK;
					break;
				case POST_GAME:
					newGameState = EGameStateNeutral.POST_GAME;
					break;
				case PENALTY_SHOOTOUT:
					break;
				default:
					break;
			}
			switch (refereeMsg.getCommand())
			{
				case DIRECT_FREE_BLUE:
					newGameState = EGameStateNeutral.DIRECT_KICK_BLUE;
					break;
				case DIRECT_FREE_YELLOW:
					newGameState = EGameStateNeutral.DIRECT_KICK_YELLOW;
					break;
				case FORCE_START:
					newGameState = EGameStateNeutral.RUNNING;
					break;
				case GOAL_BLUE:
					break;
				case GOAL_YELLOW:
					break;
				case HALT:
					newGameState = EGameStateNeutral.HALTED;
					break;
				case INDIRECT_FREE_BLUE:
					newGameState = EGameStateNeutral.INDIRECT_KICK_BLUE;
					break;
				case INDIRECT_FREE_YELLOW:
					newGameState = EGameStateNeutral.INDIRECT_KICK_YELLOW;
					break;
				case PREPARE_KICKOFF_BLUE:
					newGameState = EGameStateNeutral.PREPARE_KICKOFF_BLUE;
					break;
				case PREPARE_KICKOFF_YELLOW:
					newGameState = EGameStateNeutral.PREPARE_KICKOFF_YELLOW;
					break;
				case NORMAL_START:
					if ((lastGameState == EGameStateNeutral.STOPPED) || (lastGameState == EGameStateNeutral.UNKNOWN))
					{
						// NORMAL_START is not allowed after STOP, but assistant ref may pressed wrong button...
						newGameState = EGameStateNeutral.RUNNING;
					} else if (lastGameState == EGameStateNeutral.PREPARE_KICKOFF_BLUE)
					{
						newGameState = EGameStateNeutral.KICKOFF_BLUE;
					} else if (lastGameState == EGameStateNeutral.PREPARE_KICKOFF_YELLOW)
					{
						newGameState = EGameStateNeutral.KICKOFF_YELLOW;
					} else if (lastGameState == EGameStateNeutral.PREPARE_PENALTY_BLUE)
					{
						newGameState = EGameStateNeutral.PENALTY_BLUE;
					} else if (lastGameState == EGameStateNeutral.PREPARE_PENALTY_YELLOW)
					{
						newGameState = EGameStateNeutral.PENALTY_YELLOW;
					}
					break;
				case PREPARE_PENALTY_BLUE:
					newGameState = EGameStateNeutral.PREPARE_PENALTY_BLUE;
					break;
				case PREPARE_PENALTY_YELLOW:
					newGameState = EGameStateNeutral.PREPARE_PENALTY_YELLOW;
					break;
				case STOP:
					newGameState = EGameStateNeutral.STOPPED;
					break;
				case TIMEOUT_BLUE:
					newGameState = EGameStateNeutral.TIMEOUT_BLUE;
					break;
				case TIMEOUT_YELLOW:
					newGameState = EGameStateNeutral.TIMEOUT_YELLOW;
					break;
				case BALL_PLACEMENT_BLUE:
					newGameState = EGameStateNeutral.BALL_PLACEMENT_BLUE;
					break;
				case BALL_PLACEMENT_YELLOW:
					newGameState = EGameStateNeutral.BALL_PLACEMENT_YELLOW;
					break;
				default:
					throw new IllegalStateException();
			}
			
			// remember ball position on certain situations
			switch (refereeMsg.getCommand())
			{
				case DIRECT_FREE_BLUE:
				case DIRECT_FREE_YELLOW:
				case INDIRECT_FREE_BLUE:
				case INDIRECT_FREE_YELLOW:
				case NORMAL_START:
					ballPosOnPrepare = ballPos;
					break;
				default:
					ballPosOnPrepare = null;
			}
		} else
		{
			switch (lastGameState)
			{
				case PENALTY_BLUE:
				case PENALTY_YELLOW:
				case KICKOFF_BLUE:
				case KICKOFF_YELLOW:
				case INDIRECT_KICK_BLUE:
				case INDIRECT_KICK_YELLOW:
				case DIRECT_KICK_BLUE:
				case DIRECT_KICK_YELLOW:
					if ((ballPosOnPrepare != null)
							&& (GeoMath.distancePP(ballPos, ballPosOnPrepare) > BALL_MOVED_DISTANCE_TOL))
					{
						ballPosOnPrepare = null;
						newGameState = EGameStateNeutral.RUNNING;
						break;
					}
				case HALTED:
					break;
				case RUNNING:
					break;
				case STOPPED:
					break;
				case TIMEOUT_BLUE:
					break;
				case TIMEOUT_YELLOW:
					break;
				case UNKNOWN:
					break;
				case BREAK:
					break;
				case POST_GAME:
					break;
				case BALL_PLACEMENT_BLUE:
					break;
				case BALL_PLACEMENT_YELLOW:
					break;
				case PREPARE_KICKOFF_BLUE:
					break;
				case PREPARE_KICKOFF_YELLOW:
					break;
				case PREPARE_PENALTY_BLUE:
					break;
				case PREPARE_PENALTY_YELLOW:
					break;
				default:
					throw new IllegalStateException();
			}
		}
		lastGameState = newGameState;
		wfw.setGameState(newGameState);
	}
}
