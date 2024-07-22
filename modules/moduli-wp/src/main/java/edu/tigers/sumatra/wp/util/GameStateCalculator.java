/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Stage;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;


/**
 * Check in which game state we are by consulting new referee messages and the ball position
 */
@Log4j2
public class GameStateCalculator
{
	@Configurable(comment = "Ball movement tolerance", defValue = "50")
	private static double ballMovedDistanceTol = 50;

	static
	{
		ConfigRegistration.registerClass("wp", GameStateCalculator.class);
	}

	private IVector2 ballPosOnPrepare;
	private final TimestampTimer ballMovedTimer = new TimestampTimer(0.03);
	private long lastRefMsgCounter;
	private GameState lastGameState;
	private Command lastRefCmd;


	public GameStateCalculator()
	{
		reset();
	}


	/**
	 * reset state
	 */
	public void reset()
	{
		lastGameState = GameState.empty().build();
		lastRefCmd = Command.STOP;
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
			log.debug("Referee message with new command: {}", refereeMsg);
			lastRefMsgCounter = refereeMsg.getCmdCounter();
			processCommand(refereeMsg.getCommand(), lastRefCmd, builder);
			storeBallPosition(refereeMsg.getCommand(), ballPos);
			lastRefCmd = refereeMsg.getCommand();
		}

		processNextCommand(refereeMsg.getNextCommand(), refereeMsg.getCommand(), builder);
		processStage(refereeMsg.getStage(), builder);
		processBallMovement(ballPos, builder, timestamp);
		processActionTimeRemaining(builder, refereeMsg.getCurrentActionTimeRemaining());

		builder.withBallPlacementPosition(refereeMsg.getBallPlacementPosNeutral());

		// we build this with NEUTRAL ourTeam because we don't know our team yet
		return builder.withOurTeam(ETeamColor.NEUTRAL).build();
	}


	private void processStage(final Stage stage, final GameState.GameStateBuilder builder)
	{
		builder.withPenaltyShootout(false);
		switch (stage)
		{
			case NORMAL_HALF_TIME, EXTRA_TIME_BREAK, EXTRA_HALF_TIME, PENALTY_SHOOTOUT_BREAK ->
					builder.withState(EGameState.BREAK).withForTeam(ETeamColor.NEUTRAL);
			case POST_GAME -> builder.withState(EGameState.POST_GAME).withForTeam(ETeamColor.NEUTRAL);
			case PENALTY_SHOOTOUT -> builder.withPenaltyShootout(true);
			default ->
			{
				if (lastGameState.getState() == EGameState.BREAK || lastGameState.getState() == EGameState.POST_GAME)
				{
					builder.withState(EGameState.HALT).withForTeam(ETeamColor.NEUTRAL);
				}
			}
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
	private EGameState commandToState(final Command command)
	{
		return switch (command)
		{
			case HALT -> EGameState.HALT;
			case STOP -> EGameState.STOP;
			case NORMAL_START, FORCE_START -> EGameState.RUNNING;
			case PREPARE_KICKOFF_YELLOW, PREPARE_KICKOFF_BLUE -> EGameState.PREPARE_KICKOFF;
			case PREPARE_PENALTY_YELLOW, PREPARE_PENALTY_BLUE -> EGameState.PREPARE_PENALTY;
			case DIRECT_FREE_YELLOW, DIRECT_FREE_BLUE -> EGameState.DIRECT_FREE;
			case INDIRECT_FREE_YELLOW, INDIRECT_FREE_BLUE -> EGameState.INDIRECT_FREE;
			case TIMEOUT_YELLOW, TIMEOUT_BLUE -> EGameState.TIMEOUT;
			case BALL_PLACEMENT_YELLOW, BALL_PLACEMENT_BLUE -> EGameState.BALL_PLACEMENT;
			default -> null;
		};
	}


	// Splitting this in multiple methods is not reasonable
	private ETeamColor commandToTeam(final Command command)
	{
		return switch (command)
		{
			case PREPARE_KICKOFF_YELLOW, PREPARE_PENALTY_YELLOW, DIRECT_FREE_YELLOW, INDIRECT_FREE_YELLOW, TIMEOUT_YELLOW, BALL_PLACEMENT_YELLOW ->
					ETeamColor.YELLOW;
			case PREPARE_KICKOFF_BLUE, PREPARE_PENALTY_BLUE, DIRECT_FREE_BLUE, INDIRECT_FREE_BLUE, TIMEOUT_BLUE, BALL_PLACEMENT_BLUE ->
					ETeamColor.BLUE;
			case HALT, STOP, NORMAL_START, FORCE_START -> ETeamColor.NEUTRAL;
			default -> null;
		};
	}


	private EGameState normalStartToState(final Command lastCommand)
	{
		return switch (lastCommand)
		{
			case PREPARE_KICKOFF_BLUE, PREPARE_KICKOFF_YELLOW -> EGameState.KICKOFF;
			case PREPARE_PENALTY_BLUE, PREPARE_PENALTY_YELLOW -> EGameState.PENALTY;
			default -> EGameState.RUNNING;
		};
	}


	private void storeBallPosition(final Command command, final IVector2 ballPos)
	{
		ballPosOnPrepare = switch (command)
		{
			case DIRECT_FREE_BLUE, DIRECT_FREE_YELLOW, INDIRECT_FREE_BLUE, INDIRECT_FREE_YELLOW, NORMAL_START -> ballPos;
			default -> null;
		};
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


	private void processActionTimeRemaining(GameState.GameStateBuilder builder, double currentActionTimeRemaining)
	{
		if ((lastGameState.isFreeKick() || lastGameState.isKickoff()) && currentActionTimeRemaining < 0)
		{
			builder.withState(EGameState.RUNNING).withForTeam(ETeamColor.NEUTRAL);
		}
	}
}
