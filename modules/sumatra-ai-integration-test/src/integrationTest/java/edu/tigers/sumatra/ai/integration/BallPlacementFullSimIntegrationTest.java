/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.ai.integration.stopcondition.BotsNotMovingStopCondition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import org.junit.Test;


public class BallPlacementFullSimIntegrationTest extends AFullSimIntegrationTest
{
	private static final int maxOwnBots = 3;
	private static final int maxOppBots = 3;


	@Test
	public void ballPlacementCornerToCenterMultiple()
	{
		runBallPlacement(
				Geometry.getFieldWBorders().getCorner(IRectangle.ECorner.BOTTOM_LEFT),
				Vector2.zero(),
				maxOwnBots
		);
	}


	@Test
	public void ballPlacementCornerToCenterSingle()
	{
		runBallPlacement(
				Geometry.getFieldWBorders().getCorner(IRectangle.ECorner.BOTTOM_LEFT),
				Vector2.zero(),
				1
		);
	}


	@Test
	public void ballPlacementCornerToOppositeCornerMultiple()
	{
		runBallPlacement(
				Geometry.getFieldWBorders().getCorner(IRectangle.ECorner.BOTTOM_LEFT),
				Geometry.getField().getCorner(IRectangle.ECorner.TOP_LEFT).addMagnitude(Vector2.fromXY(-200, -200)),
				maxOwnBots
		);
	}


	@Test
	public void ballPlacementCornerToOppositeCornerSingle()
	{
		runBallPlacement(
				Geometry.getFieldWBorders().getCorner(IRectangle.ECorner.BOTTOM_LEFT),
				Geometry.getField().getCorner(IRectangle.ECorner.TOP_LEFT).addMagnitude(Vector2.fromXY(-200, -200)),
				1
		);
	}


	@Test
	public void ballPlacementGoalCornerToCenterMultiple()
	{
		runBallPlacement(
				Geometry.getGoalTheir().getCorners().getFirst(),
				Vector2.zero(),
				maxOwnBots
		);
	}


	@Test
	public void ballPlacementGoalCornerToCenterSingle()
	{
		runBallPlacement(
				Geometry.getGoalTheir().getCorners().getFirst(),
				Vector2.zero(),
				1
		);
	}


	@Test
	public void ballPlacementTouchBoundaryToCenterMultiple()
	{
		runBallPlacement(
				Vector2.fromXY(2000, Geometry.getFieldWidth() / 2 + Geometry.getBoundaryWidth()),
				Vector2.zero(),
				maxOwnBots
		);
	}


	@Test
	public void ballPlacementTouchBoundaryToCenterSingle()
	{
		runBallPlacement(
				Vector2.fromXY(2000, Geometry.getFieldWidth() / 2 + Geometry.getBoundaryWidth()),
				Vector2.zero(),
				1
		);
	}


	@Test
	public void ballPlacementAlmostTouchBoundaryToCenterMultiple()
	{
		runBallPlacement(
				Vector2.fromXY(2000,
						Geometry.getFieldWidth() / 2 + Geometry.getBoundaryWidth() - 2 * Geometry.getBotRadius()),
				Vector2.zero(),
				maxOwnBots
		);
	}


	@Test
	public void ballPlacementAlmostTouchBoundaryToCenterSingle()
	{
		runBallPlacement(
				Vector2.fromXY(2000,
						Geometry.getFieldWidth() / 2 + Geometry.getBoundaryWidth() - 2 * Geometry.getBotRadius()),
				Vector2.zero(),
				1
		);
	}


	@Test
	public void ballPlacementInsideGoalToCornerMultiple()
	{
		runBallPlacement(
				Geometry.getGoalTheir().getCenter().addMagnitude(Vector2.fromX(Geometry.getGoalTheir().getDepth())),
				Geometry.getGoalTheir().getCenter().addMagnitude(Vector2.fromXY(-200, Geometry.getFieldWidth() / 2 - 200)),
				maxOwnBots
		);
	}


	@Test
	public void ballPlacementInsideGoalToCornerSingle()
	{
		runBallPlacement(
				Geometry.getGoalTheir().getCenter().addMagnitude(Vector2.fromX(Geometry.getGoalTheir().getDepth())),
				Geometry.getGoalTheir().getCenter().addMagnitude(Vector2.fromXY(-200, Geometry.getFieldWidth() / 2 - 200)),
				1
		);
	}


	private void runBallPlacement(IVector2 source, IVector2 target, int numBots)
	{
		initSimulation(new SnapshotGenerator()
				.maintenance(ETeamColor.BLUE, numBots)
				.maintenance(ETeamColor.YELLOW, maxOppBots)
				.ballPos(source)
				.snapshotBuilder()
				.command(SslGcRefereeMessage.Referee.Command.STOP)
				.placementPos(target)
				.autoContinue(false)
				.build());
		defaultSimTimeBlocker(0.5)
				.addStopCondition(new BotsNotMovingStopCondition(0.1))
				.await();
		sendRefereeCommand(SslGcRefereeMessage.Referee.Command.BALL_PLACEMENT_BLUE);
		defaultSimTimeBlocker(0.1)
				.await();
		// 30s is allowed in the real game, but this is only simulated, and we really should not need the full 30s
		defaultSimTimeBlocker(20)
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
