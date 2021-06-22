/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Stage;
import edu.tigers.sumatra.time.TimestampTimer;

import java.util.Optional;


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
	private final TimestampTimer ballMovedTimer = new TimestampTimer(0.03);
	private long lastRefMsgCounter = -1;
	private GameState lastGameState = GameState.empty().build();
	private Command lastRefCmd = Command.STOP;


	/**
	 * reset state
	 */
	public void reset()
	{
		reset(GameState.empty().build());
	}


	/**
	 * reset state
	 */
	public void reset(GameState gameState)
	{
		lastGameState = gameState;
		lastRefMsgCounter = -1;
		ballPosOnPrepare = null;
		ballMovedTimer.reset();
	}


	/**
	 * @param refereeMsg latest referee message
	 * @param ballPos    current ball position
	 * @return next game state
	 */
	public GameState getNextGameState(final RefereeMsg refereeMsg, final IVector2 ballPos, long timestamp)
	{
		GameState nextGameState = calcGameState(refereeMsg, ballPos, timestamp);
		lastGameState = nextGameState;
		return nextGameState;
	}


	private GameState calcGameState(final RefereeMsg refereeMsg, final IVector2 ballPos, long timestamp)
	{
		GameState.GameStateBuilder builder = lastGameState.toBuilder();

		if (refereeMsg.getCmdCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = refereeMsg.getCmdCounter();
			processCommand(refereeMsg.getCommand(), lastRefCmd, builder);
			storeBallPosition(refereeMsg.getCommand(), ballPos);
			lastRefCmd = refereeMsg.getCommand();
		}

		processNextCommand(refereeMsg.getNextCommand(), refereeMsg.getCommand(), builder);
		builder.withBallPlacementPosition(refereeMsg.getBallPlacementPosNeutral());
		processStage(refereeMsg.getStage(), builder);
		processBallMovement(ballPos, builder, timestamp);

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


	private void processStage(final Stage stage, final GameState.GameStateBuilder builder)
	{
		builder.withPenaltyShootout(false);
		switch (stage)
		{
			case NORMAL_HALF_TIME:
			case EXTRA_TIME_BREAK:
			case EXTRA_HALF_TIME:
			case PENALTY_SHOOTOUT_BREAK:
				builder.withState(EGameState.BREAK).withForTeam(ETeamColor.NEUTRAL);
				break;
			case POST_GAME:
				builder.withState(EGameState.POST_GAME).withForTeam(ETeamColor.NEUTRAL);
				break;
			case PENALTY_SHOOTOUT:
				builder.withPenaltyShootout(true);
				break;
			default:
				// ignore stage
				break;
		}
	}


	private void processCommand(final Command command, final Command lastCommand,
			final GameState.GameStateBuilder builder)
	{
		if (command == Command.NORMAL_START)
		{
			builder.withState(normalStartToState(lastCommand));
			builder.withForTeam(Optional.ofNullable(commandToTeam(lastCommand)).orElse(ETeamColor.NEUTRAL));
		} else if (command != null)
		{
			Optional.ofNullable(commandToState(command)).ifPresent(builder::withState);
			Optional.ofNullable(commandToTeam(command)).ifPresent(builder::withForTeam);
		}
	}


	private void processNextCommand(final Command nextCommand, final Command currentCommand,
			final GameState.GameStateBuilder builder)
	{
		if (nextCommand == Command.NORMAL_START)
		{
			builder.withNextState(normalStartToState(currentCommand));
			builder.withNextForTeam(commandToTeam(currentCommand));
		} else if (nextCommand != null)
		{
			builder.withNextState(commandToState(nextCommand));
			builder.withNextForTeam(commandToTeam(nextCommand));
		} else
		{
			builder.withNextState(null);
			builder.withNextForTeam(ETeamColor.NEUTRAL);
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
				return ETeamColor.NEUTRAL;
			default:
				return null;
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


	private void processBallMovement(IVector2 ballPos, GameState.GameStateBuilder builder, long timestamp)
	{
		if (ballPosOnPrepare == null || lastGameState.isPenaltyOrPreparePenalty())
		{
			return;
		}

		if (ballPos.distanceTo(ballPosOnPrepare) > ballMovedDistanceTol)
		{
			ballMovedTimer.update(timestamp);
			if (ballMovedTimer.isTimeUp(timestamp))
			{
				builder.withState(EGameState.RUNNING).withForTeam(ETeamColor.NEUTRAL);
				ballPosOnPrepare = null;
			}
		} else
		{
			ballMovedTimer.reset();
		}
	}
}
