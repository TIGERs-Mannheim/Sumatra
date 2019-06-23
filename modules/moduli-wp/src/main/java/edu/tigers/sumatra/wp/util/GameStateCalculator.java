/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * Check in which game state we are by consulting new referee messages and the ball position
 * 
 * @author AndreR <andre@ryll.cc>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GameStateCalculator
{
	private static final double	BALL_MOVED_DISTANCE_TOL	= 50;
	private IVector2					ballPosOnPrepare			= null;
	private long						lastRefMsgCounter			= -1;
	private GameState					lastGameState				= GameState.Builder.empty().build();
	private Command					lastRefCmd					= Command.STOP;
	
	
	/**
	 * reset state
	 */
	public void reset()
	{
		lastGameState = GameState.Builder.empty().build();
		lastRefMsgCounter = -1;
		ballPosOnPrepare = null;
	}
	
	
	/**
	 * @param refereeMsg latest referee message
	 * @param ballPos current ball position
	 * @return next game state
	 */
	public GameState getNextGameState(final RefereeMsg refereeMsg, final IVector2 ballPos)
	{
		GameState nextGameState = calcGameState(refereeMsg, ballPos);
		lastGameState = nextGameState;
		return nextGameState;
	}
	
	
	private boolean isNewRefereeMsg(final RefereeMsg refereeMsg)
	{
		return refereeMsg.getCommandCounter() != lastRefMsgCounter;
	}
	
	
	private GameState calcGameState(final RefereeMsg refereeMsg, final IVector2 ballPos)
	{
		GameState.Builder builder = GameState.Builder.create().withGameState(lastGameState);
		
		if (isNewRefereeMsg(refereeMsg))
		{
			lastRefMsgCounter = refereeMsg.getCommandCounter();
			
			processCommand(refereeMsg.getCommand(), lastRefCmd, builder);
			processBallPlacement(refereeMsg.getCommand(), refereeMsg.getBallPlacementPos(), builder);
			processStage(refereeMsg.getStage(), builder);
			storeBallPosition(refereeMsg.getCommand(), ballPos);
			
			lastRefCmd = refereeMsg.getCommand();
		}

		if (refereeMsg.getStage() != Stage.PENALTY_SHOOTOUT) {
			processBallMovement(ballPos, builder);
		}
		
		// we build this with NEUTRAL ourTeam because we don't know our team yet
		return builder.withOurTeam(ETeamColor.NEUTRAL).build();
	}
	
	
	private void processStage(final Stage stage, final GameState.Builder builder)
	{
		builder.withPenalyShootout(false);
		switch (stage)
		{
			case NORMAL_HALF_TIME:
			case EXTRA_TIME_BREAK:
			case EXTRA_HALF_TIME:
			case PENALTY_SHOOTOUT_BREAK:
				builder.withState(EGameState.BREAK).forTeam(ETeamColor.NEUTRAL);
				break;
			case POST_GAME:
				builder.withState(EGameState.POST_GAME).forTeam(ETeamColor.NEUTRAL);
				break;
			case PENALTY_SHOOTOUT:
				builder.withPenalyShootout(true);
				break;
			default:
				// ignore stage
				break;
		}
	}
	
	
	// Splitting this in multiple methods is not reasonable
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private void processCommand(final Command command, final Command lastCommand, final GameState.Builder builder)
	{
		switch (command)
		{
			case BALL_PLACEMENT_BLUE:
				builder.withState(EGameState.BALL_PLACEMENT).forTeam(ETeamColor.BLUE);
				break;
			case BALL_PLACEMENT_YELLOW:
				builder.withState(EGameState.BALL_PLACEMENT).forTeam(ETeamColor.YELLOW);
				break;
			case DIRECT_FREE_BLUE:
				builder.withState(EGameState.DIRECT_FREE).forTeam(ETeamColor.BLUE);
				break;
			case DIRECT_FREE_YELLOW:
				builder.withState(EGameState.DIRECT_FREE).forTeam(ETeamColor.YELLOW);
				break;
			case FORCE_START:
				builder.withState(EGameState.RUNNING).forTeam(ETeamColor.NEUTRAL);
				break;
			case STOP:
			case GOAL_BLUE:
			case GOAL_YELLOW:
				builder.withState(EGameState.STOP).forTeam(ETeamColor.NEUTRAL);
				break;
			case HALT:
				builder.withState(EGameState.HALT).forTeam(ETeamColor.NEUTRAL);
				break;
			case INDIRECT_FREE_BLUE:
				builder.withState(EGameState.INDIRECT_FREE).forTeam(ETeamColor.BLUE);
				break;
			case INDIRECT_FREE_YELLOW:
				builder.withState(EGameState.INDIRECT_FREE).forTeam(ETeamColor.YELLOW);
				break;
			case NORMAL_START:
				processNormalStart(lastCommand, builder);
				break;
			case PREPARE_KICKOFF_BLUE:
				builder.withState(EGameState.PREPARE_KICKOFF).forTeam(ETeamColor.BLUE);
				break;
			case PREPARE_KICKOFF_YELLOW:
				builder.withState(EGameState.PREPARE_KICKOFF).forTeam(ETeamColor.YELLOW);
				break;
			case PREPARE_PENALTY_BLUE:
				builder.withState(EGameState.PREPARE_PENALTY).forTeam(ETeamColor.BLUE);
				break;
			case PREPARE_PENALTY_YELLOW:
				builder.withState(EGameState.PREPARE_PENALTY).forTeam(ETeamColor.YELLOW);
				break;
			case TIMEOUT_BLUE:
				builder.withState(EGameState.TIMEOUT).forTeam(ETeamColor.BLUE);
				break;
			case TIMEOUT_YELLOW:
				builder.withState(EGameState.TIMEOUT).forTeam(ETeamColor.YELLOW);
				break;
			default:
				break;
		}
	}
	
	
	private void processNormalStart(final Command lastCommand, final GameState.Builder builder)
	{
		switch (lastCommand)
		{
			case PREPARE_KICKOFF_BLUE:
				builder.withState(EGameState.KICKOFF).forTeam(ETeamColor.BLUE);
				break;
			case PREPARE_KICKOFF_YELLOW:
				builder.withState(EGameState.KICKOFF).forTeam(ETeamColor.YELLOW);
				break;
			case PREPARE_PENALTY_BLUE:
				builder.withState(EGameState.PENALTY).forTeam(ETeamColor.BLUE);
				break;
			case PREPARE_PENALTY_YELLOW:
				builder.withState(EGameState.PENALTY).forTeam(ETeamColor.YELLOW);
				break;
			default:
				builder.withState(EGameState.RUNNING).forTeam(ETeamColor.NEUTRAL);
				break;
		}
	}
	
	
	private void storeBallPosition(final Command command, final IVector2 ballPos)
	{
		switch (command)
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
				break;
		}
	}
	
	
	private void processBallMovement(final IVector2 ballPos, final GameState.Builder builder)
	{
		if (ballPosOnPrepare == null)
		{
			return;
		}
		
		if (ballPos.distanceTo(ballPosOnPrepare) > BALL_MOVED_DISTANCE_TOL)
		{
			builder.withState(EGameState.RUNNING).forTeam(ETeamColor.NEUTRAL);
			ballPosOnPrepare = null;
		}
	}
	
	
	private void processBallPlacement(final Command command, final IVector2 placePos, final GameState.Builder builder)
	{
		switch (command)
		{
			case BALL_PLACEMENT_BLUE:
			case BALL_PLACEMENT_YELLOW:
				builder.withBallPlacementPosition(placePos);
				break;
			default:
				break;
		}
	}
}
