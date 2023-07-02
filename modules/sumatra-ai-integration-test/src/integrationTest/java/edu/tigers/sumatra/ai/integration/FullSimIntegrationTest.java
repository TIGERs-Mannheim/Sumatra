/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.ai.integration.stopcondition.BotsNotMovingStopCondition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Run tests in a full simulation environment
 */
public class FullSimIntegrationTest extends AFullSimIntegrationTest
{
	/**
	 * Check if we can handle a force start: Robots should move, no rules should be violated. That's it.
	 */
	@Test
	public void forceStart()
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
				.addStopCondition(this::ballLeftField)
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
	public void forceStartWithoutOpponents()
	{
		initSimulation("snapshots/stoppedGame11vs0.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.FORCE_START);
		defaultSimTimeBlocker(10)
				.addStopCondition(this::ballLeftField)
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
	public void kickoff()
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
				.addStopCondition(this::ballLeftField)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(2)
				.addStopCondition(this::ballLeftField)
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if we shoot a goal on kick_off without opponents
	 */
	@Test
	public void kickoffWithoutOpponents()
	{
		initSimulation("snapshots/stoppedGame11vs0.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.PREPARE_KICKOFF_YELLOW);
		defaultSimTimeBlocker(11)
				.addStopCondition(this::ballLeftField)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(2)
				.addStopCondition(this::ballLeftField)
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
	public void goalKick()
	{
		initSimulation("snapshots/goalKick11vs11.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.DIRECT_FREE_BLUE);
		defaultSimTimeBlocker(6)
				.addStopCondition(this::ballLeftField)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(2)
				.addStopCondition(this::ballLeftField)
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
	public void cornerKick()
	{
		initSimulation("snapshots/cornerKick11vs11.json");
		defaultSimTimeBlocker(1)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(Command.DIRECT_FREE_YELLOW);
		defaultSimTimeBlocker(6)
				.addStopCondition(this::ballLeftField)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		defaultSimTimeBlocker(2)
				.addStopCondition(this::ballLeftField)
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
	public void penalty()
	{
		initSimulation("snapshots/penalty11vs11.json");
		defaultSimTimeBlocker(30)
				.addStopCondition(this::ballLeftField)
				.addStopCondition(this::gameBallPlacement)
				.await();

		// game should be either halted (goal) or in ball_placement (failed penalty)
		assertThat(List.of(EGameState.HALT, EGameState.BALL_PLACEMENT))
				.contains(lastWorldFrameWrapper.getGameState().getState());

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	/**
	 * Check if robots move correctly during stop, without violating rules
	 */
	@Test
	public void stopBallOutsideField()
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
	public void ballPlacementCorner()
	{
		runBallPlacement(
				Geometry.getField().getCorner(IRectangle.ECorner.BOTTOM_LEFT).subtractNew(Vector2.fromXY(200, 200)),
				Vector2.fromXY(2900, -900)
		);
	}


	@Test
	public void ballPlacementGoal()
	{
		runBallPlacement(
				Geometry.getGoalTheir().getLeftPost().addNew(Vector2.fromXY(100, -100)),
				Vector2.fromXY(2900, 1500)
		);
	}


	private void runBallPlacement(IVector2 source, IVector2 target)
	{
		initSimulation(new SnapshotGenerator()
				.maintenance(ETeamColor.BLUE, 6)
				.maintenance(ETeamColor.YELLOW, 6)
				.ballPos(source)
				.snapshotBuilder()
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
		defaultSimTimeBlocker(30)
				.addStopCondition(this::gameStopped)
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
