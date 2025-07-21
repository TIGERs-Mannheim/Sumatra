/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.ai.integration.stopcondition.BotsNotMovingStopCondition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Run tests in a full simulation environment
 */
class FullSimIntegrationTest extends AFullSimIntegrationTest
{
	/**
	 * Check if we can handle a force start: Robots should move, no rules should be violated. That's it.
	 */
	@Test
	void forceStart()
	{
		initSimulation("snapshots/stoppedGame11vs11.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.FORCE_START);
		defaultSimTimeBlocker(1)
				.await();

		ICircle aroundBallCircle = Circle.createCircle(
				lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos(),
				RuleConstraints.getStopRadius() + Geometry.getBotRadius());
		Map<ETeamColor, List<ITrackedBot>> botsNearBall = lastWorldFrameWrapper.getSimpleWorldFrame().getBots().values()
				.stream()
				.filter(b -> aroundBallCircle.isPointInShape(b.getPos()))
				.collect(Collectors.groupingBy(ITrackedBot::getTeamColor));
		assertThat(botsNearBall).containsKeys(ETeamColor.YELLOW, ETeamColor.BLUE);
		assertThat(botsNearBall.get(ETeamColor.YELLOW)).hasSizeGreaterThan(0);
		assertThat(botsNearBall.get(ETeamColor.BLUE)).hasSizeGreaterThan(0);

		defaultSimTimeBlocker(6)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(new BallMovedStopCondition(100.0, 0.1))
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if we shoot a goal on force_start, when there are no opponents
	 */
	@Test
	void forceStartWithoutOpponents()
	{
		initSimulation("snapshots/stoppedGame11vs0.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.FORCE_START);
		defaultSimTimeBlocker(10)
				.addStopCondition(new BallLeftFieldStopCondition())
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		assertGameEvent(EGameEvent.POSSIBLE_GOAL);
		success();
	}


	/**
	 * Check if we can perform a kickoff. If the game state switches to running without any other events in between,
	 * we are good.
	 */
	@Test
	void kickoff()
	{
		initSimulation(new SnapshotGenerator()
				.maintenance(ETeamColor.BLUE, 11)
				.maintenance(ETeamColor.YELLOW, 11)
				.snapshotBuilder()
				.command(Command.STOP)
				.build());
		defaultSimTimeBlocker(2)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.PREPARE_KICKOFF_BLUE);
		defaultSimTimeBlocker(11)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(new GameStateStopCondition(EGameState.RUNNING))
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(5)
				.addStopCondition(new BallLeftFieldStopCondition())
				.await();

		assertSuccessfulFirstPass(EAiTeam.BLUE); // Blue is team that has the kickoff
		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if we shoot a goal on kick_off without opponents
	 */
	@Test
	void kickoffWithoutOpponents()
	{
		initSimulation("snapshots/stoppedGame11vs0.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.PREPARE_KICKOFF_YELLOW);
		defaultSimTimeBlocker(11)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(new GameStateStopCondition(EGameState.RUNNING))
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(5)
				.addStopCondition(new BallLeftFieldStopCondition())
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		assertGameEvent(EGameEvent.POSSIBLE_GOAL);
		success();
	}


	/**
	 * Check if we can do a goal kick
	 */
	@Test
	void goalKick()
	{
		initSimulation("snapshots/goalKick11vs11.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.DIRECT_FREE_BLUE);
		defaultSimTimeBlocker(6)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(new GameStateStopCondition(EGameState.RUNNING))
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(2)
				.addStopCondition(new BallLeftFieldStopCondition())
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if we can do a corner kick
	 */
	@Test
	void cornerKick()
	{
		initSimulation("snapshots/cornerKick11vs11.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.DIRECT_FREE_YELLOW);
		defaultSimTimeBlocker(6)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(new GameStateStopCondition(EGameState.RUNNING))
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(2)
				.addStopCondition(new BallLeftFieldStopCondition())
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if penalty kicks work
	 */
	@Test
	void penalty()
	{
		initSimulation("snapshots/penalty11vs11.json");
		defaultSimTimeBlocker(30)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(new GameStateStopCondition(EGameState.BALL_PLACEMENT))
				.await();

		// game should be either halted (goal) or in ball_placement (failed penalty)
		assertThat(lastWorldFrameWrapper.getGameState().getState())
				.isIn(EGameState.HALT, EGameState.BALL_PLACEMENT);

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if robots move correctly during stop, without violating rules
	 */
	@Test
	void stopBallOutsideField()
	{
		initSimulation(new SnapshotGenerator()
				.maintenance(ETeamColor.BLUE, 11)
				.maintenance(ETeamColor.YELLOW, 11)
				.ballPos(Vector2.fromXY(0, Geometry.getFieldWidth() / 2 + 1000))
				.snapshotBuilder()
				.command(Command.STOP)
				.build());

		defaultSimTimeBlocker(7)
				.addStopCondition(new BotsNotMovingStopCondition(0.5))
				.await();

		assertThat(Geometry.getField().isPointInShape(lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos()))
				.as("The ball should still be outside of the field")
				.isFalse();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	@Test
	void ballPlacementCorner()
	{
		runBallPlacement(
				Geometry.getField().getCorner(IRectangle.ECorner.BOTTOM_LEFT).subtractNew(Vector2.fromXY(200, 200)),
				Vector2.fromXY(2900, -900)
		);
	}


	@Test
	void ballPlacementGoal()
	{
		runBallPlacement(
				Geometry.getGoalTheir().getLeftPost().addNew(Vector2.fromXY(100, -100)),
				Vector2.fromXY(2900, 1500)
		);
	}


	/**
	 * Perform ball placement when many bots are close to penalty area.
	 * Path planning gets complicated for multiple robots.
	 */
	@Test
	void ballPlacementCrowded()
	{
		initSimulation("snapshots/ballPlacementCrowded.json");
		testBallPlacement();
	}


	/**
	 * Perform ball placement when many bots are close to penalty area.
	 * Path planning gets complicated for multiple robots.
	 */
	@Test
	void ballPlacementAtBorder()
	{
		initSimulation("snapshots/ballPlacementAtBorder.json");
		testBallPlacement();
	}


	private void testBallPlacement()
	{
		defaultSimTimeBlocker(1)
				.await();
		defaultSimTimeBlocker(30)
				.addStopCondition(new GameStateStopCondition(EGameState.STOP))
				.await();

		// game should be either halted (goal) or in ball_placement (failed penalty)
		assertThat(lastWorldFrameWrapper.getGameState().getState())
				.isNotEqualTo(EGameState.BALL_PLACEMENT);

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	private void runBallPlacement(IVector2 source, IVector2 target)
	{
		initSimulation(new SnapshotGenerator()
				.maintenance(ETeamColor.BLUE, 6)
				.maintenance(ETeamColor.YELLOW, 6)
				.ballPos(source)
				.snapshotBuilder()
				.stage(SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF)
				.command(Command.STOP)
				.placementPos(target)
				.autoContinue(false)
				.build());
		defaultSimTimeBlocker(5)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.BALL_PLACEMENT_BLUE);
		defaultSimTimeBlocker(5)
				.await();
		// 30s is allowed in the real game, but this is only simulated we really should not need the full 30s
		defaultSimTimeBlocker(15)
				.addStopCondition(new GameStateStopCondition(EGameState.STOP))
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		assertGameEvent(EGameEvent.PLACEMENT_SUCCEEDED);
		assertNotGameEvent(EGameEvent.PLACEMENT_FAILED);
		assertGameState(EGameState.STOP);
		success();

	}


	private AiSimTimeBlocker defaultSimTimeBlocker(double maxDuration)
	{
		return new AiSimTimeBlocker(maxDuration)
				.addStopCondition(w -> stuck);
	}
}
