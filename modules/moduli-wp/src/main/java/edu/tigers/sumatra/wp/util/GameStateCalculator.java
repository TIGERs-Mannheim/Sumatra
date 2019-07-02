/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * Check in which game state we are by consulting new referee messages and the ball position
 */
public class GameStateCalculator
{
	@Configurable(comment = "Ball movement tolerance", defValue = "50")
	private static double ballMovedDistanceTol = 50;

	static
	{
		ConfigRegistration.registerClass("wp", GameStateCalculator.class);
	}

	private IVector2 ballPosOnPrepare = null;
	private long lastRefMsgCounter = -1;
	private GameState lastGameState = GameState.Builder.empty().build();
	private Command lastRefCmd = Command.STOP;


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


	private GameState calcGameState(final RefereeMsg refereeMsg, final IVector2 ballPos)
	{
		GameState.Builder builder = GameState.Builder.create().withGameState(lastGameState);

		if (refereeMsg.getCommandCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = refereeMsg.getCommandCounter();
			processCommand(refereeMsg.getCommand(), lastRefCmd, builder);
			storeBallPosition(refereeMsg.getCommand(), ballPos);
			lastRefCmd = refereeMsg.getCommand();
		}

		processNextCommand(refereeMsg.getNextCommand(), refereeMsg.getCommand(), builder);
		builder.withBallPlacementPosition(refereeMsg.getBallPlacementPosNeutral());
		processStage(refereeMsg.getStage(), builder);

		if (refereeMsg.getStage() != Stage.PENALTY_SHOOTOUT)
		{
			processBallMovement(ballPos, builder);
		}
		if ((refereeMsg.getCommand() == Command.BALL_PLACEMENT_BLUE
				|| refereeMsg.getCommand() == Command.BALL_PLACEMENT_YELLOW)
				&& refereeMsg.getBallPlacementPosNeutral() == null)
		{
			// just to avoid NPEs
			builder.withBallPlacementPosition(Vector2.zero());
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


	private void processCommand(final Command command, final Command lastCommand, final GameState.Builder builder)
	{
		if (command == Command.NORMAL_START)
		{
			builder.withState(normalStartToState(lastCommand));
			builder.forTeam(commandToTeam(lastCommand));
		} else if (command != null)
		{
			builder.withState(commandToState(command));
			builder.forTeam(commandToTeam(command));
		}
	}


	private void processNextCommand(final Command nextCommand, final Command currentCommand,
			final GameState.Builder builder)
	{
		if (nextCommand == Command.NORMAL_START)
		{
			builder.withNextState(normalStartToState(currentCommand));
			builder.nextForTeam(commandToTeam(currentCommand));
		} else if (nextCommand != null)
		{
			builder.withNextState(commandToState(nextCommand));
			builder.nextForTeam(commandToTeam(nextCommand));
		}
	}


	// Splitting this in multiple methods is not reasonable
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private EGameState commandToState(final Command command)
	{
		switch (command)
		{
			case HALT:
				return EGameState.HALT;
			case STOP:
				return EGameState.STOP;
			case NORMAL_START:
			case FORCE_START:
				return EGameState.RUNNING;
			case PREPARE_KICKOFF_YELLOW:
			case PREPARE_KICKOFF_BLUE:
				return EGameState.PREPARE_KICKOFF;
			case PREPARE_PENALTY_YELLOW:
			case PREPARE_PENALTY_BLUE:
				return EGameState.PREPARE_PENALTY;
			case DIRECT_FREE_YELLOW:
			case DIRECT_FREE_BLUE:
				return EGameState.DIRECT_FREE;
			case INDIRECT_FREE_YELLOW:
			case INDIRECT_FREE_BLUE:
				return EGameState.INDIRECT_FREE;
			case TIMEOUT_YELLOW:
			case TIMEOUT_BLUE:
				return EGameState.TIMEOUT;
			case BALL_PLACEMENT_YELLOW:
			case BALL_PLACEMENT_BLUE:
				return EGameState.BALL_PLACEMENT;
			default:
				return null;
		}
	}


	// Splitting this in multiple methods is not reasonable
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private ETeamColor commandToTeam(final Command command)
	{
		switch (command)
		{
			case PREPARE_KICKOFF_YELLOW:
			case PREPARE_PENALTY_YELLOW:
			case DIRECT_FREE_YELLOW:
			case INDIRECT_FREE_YELLOW:
			case TIMEOUT_YELLOW:
			case BALL_PLACEMENT_YELLOW:
				return ETeamColor.YELLOW;

			case PREPARE_KICKOFF_BLUE:
			case PREPARE_PENALTY_BLUE:
			case DIRECT_FREE_BLUE:
			case INDIRECT_FREE_BLUE:
			case TIMEOUT_BLUE:
			case BALL_PLACEMENT_BLUE:
				return ETeamColor.BLUE;

			case HALT:
			case STOP:
			case NORMAL_START:
			case FORCE_START:
			default:
				return ETeamColor.NEUTRAL;
		}
	}


	private EGameState normalStartToState(final Command lastCommand)
	{
		switch (lastCommand)
		{
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
				return EGameState.KICKOFF;
			case PREPARE_PENALTY_BLUE:
			case PREPARE_PENALTY_YELLOW:
				return EGameState.PENALTY;
			default:
				return EGameState.RUNNING;
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

		if (ballPos.distanceTo(ballPosOnPrepare) > ballMovedDistanceTol)
		{
			builder.withState(EGameState.RUNNING).forTeam(ETeamColor.NEUTRAL);
			ballPosOnPrepare = null;
		}
	}
}
