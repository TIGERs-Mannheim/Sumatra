/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Calculate the desired number of defenders for the ball threat.
 */
@RequiredArgsConstructor
public class NumDefendersForBallCalc extends ACalculator
{
	@Configurable(comment = "[mm] Extra margin used for goal coverage calculations during free kicks", defValue = "200.0")
	private static double freeKickExtraMarginToBall = 200.0;
	@Configurable(comment = "[rad] Max uncovered angle allowed during free kicks for each meter of distance", defValue = "0.03")
	private static double freeKickMaxUncoveredAnglePerMeterDistance = 0.03;

	@Configurable(comment = "Max number of defenders for covering the ball", defValue = "2")
	private static int maxDefendersForBallThreat = 2;

	@Configurable(comment = "Minimum number of defenders if the ball responsibility is at the defense", defValue = "1")
	private static int minDefendersForBallThreatWhenBallResponsibilityDefense = 1;


	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Hysteresis freeKickHysteresis = new Hysteresis(0, freeKickExtraMarginToBall);
	/**
	 * The number of defenders that should be assigned to the ball.
	 * This number may be higher than the total number of defenders.
	 * It is an input for determining the total number of defenders.
	 */
	@Getter
	private int numDefenderForBall = 0;


	@Override
	public void doCalc()
	{
		numDefenderForBall = numDefendersBasedOnBallPos();
	}


	private int numDefendersBasedOnBallPos()
	{
		int numBotsForBall = maxDefendersForBallThreat;

		if (needToPrepareForDirectFreeThem())
		{
			numBotsForBall = Math.min(getBallDefenderForFreeKick(), 2 * maxDefendersForBallThreat);
		} else
		{
			freeKickHysteresis.setUpper(false);
		}

		if (ballResponsibility.get() == EBallResponsibility.DEFENSE)
		{
			numBotsForBall = Math.max(minDefendersForBallThreatWhenBallResponsibilityDefense, numBotsForBall);
		}

		return numBotsForBall;
	}


	private boolean needToPrepareForDirectFreeThem()
	{
		var gameState = getAiFrame().getGameState();
		if (gameState.isFreeKickForThem())
		{
			return true;
		}
		if (gameState.isNextGameStateForUs() || gameState.isGameRunning())
		{
			return false;
		}

		return gameState.getNextState() == EGameState.DIRECT_FREE;
	}


	private int getBallDefenderForFreeKick()
	{
		var ballPos = getAiFrame().getGameState().isBallPlacement() ?
				getAiFrame().getGameState().getBallPlacementPositionForUs() :
				getBall().getPos();

		var protectionLine = defenseBallThreat.get().getProtectionLine();
		var shapes = getShapes(EAiShapesLayer.DEFENSE_NUM_DEFENDER_FOR_BALL_DEBUG);

		var leftPost = Geometry.getGoalOur().getLeftPost();
		var rightPost = Geometry.getGoalOur().getRightPost();

		final Optional<IVector2> defPosLeft;
		final Optional<IVector2> defPosRight;

		freeKickHysteresis.setUpperThreshold(freeKickExtraMarginToBall);
		if (protectionLine.isPresent() && freeKickHysteresis.update(protectionLine.get().getLength()).isUpper())
		{
			// We have enough space for the defense to go close to the ball and increase the goal coverage by this
			var defensePosCloseToBall = protectionLine.get().stepAlongPath(freeKickExtraMarginToBall);

			var ball2Defense = Vector2.fromPoints(ballPos, defensePosCloseToBall);

			var ball2LeftPostAngle = Vector2.fromPoints(ballPos, leftPost).getAngle();
			var ball2RightPostAngle = Vector2.fromPoints(ballPos, rightPost).getAngle();

			shapes.add(new DrawableLine(ballPos, leftPost, Color.GREEN));
			shapes.add(new DrawableLine(ballPos, rightPost, Color.BLUE));

			var fullCoverDistance = RuleConstraints.getStopRadius() + freeKickExtraMarginToBall;

			var freeKickMaxUncoveredAngle =
					freeKickMaxUncoveredAnglePerMeterDistance * (ball2Defense.getLength() - fullCoverDistance) / 1000.0;

			ball2LeftPostAngle = AngleMath.rotateAngle(ball2LeftPostAngle, freeKickMaxUncoveredAngle,
					ERotationDirection.COUNTER_CLOCKWISE);
			ball2RightPostAngle = AngleMath.rotateAngle(ball2RightPostAngle, freeKickMaxUncoveredAngle,
					ERotationDirection.CLOCKWISE);

			var dist = ballPos.distanceTo(Geometry.getGoalOur().getCenter());
			shapes.add(new DrawableLine(ballPos, ballPos.addNew(Vector2.fromAngleLength(ball2LeftPostAngle, dist)),
					Color.GREEN));
			shapes.add(new DrawableLine(ballPos, ballPos.addNew(Vector2.fromAngleLength(ball2RightPostAngle, dist)),
					Color.BLUE));


			if (AngleMath.rotationDirection(ball2RightPostAngle, ball2LeftPostAngle) != ERotationDirection.CLOCKWISE)
			{
				return 1;
			} else
			{
				var ball2LeftPost = Lines.halfLineFromDirection(ballPos, Vector2.fromAngle(ball2LeftPostAngle));
				var ball2RightPost = Lines.halfLineFromDirection(ballPos, Vector2.fromAngle(ball2RightPostAngle));

				var ball2DefenseNormal = Lines.lineFromDirection(defensePosCloseToBall, ball2Defense.getNormalVector());
				defPosLeft = ball2LeftPost.intersect(ball2DefenseNormal).asOptional();
				defPosRight = ball2RightPost.intersect(ball2DefenseNormal).asOptional();
			}
		} else
		{
			freeKickHysteresis.setUpper(false);
			// We need to defend with PenAreaDefenders alone
			var ballLeftPost = Lines.segmentFromPoints(ballPos, leftPost);
			var ballRightPost = Lines.segmentFromPoints(ballPos, rightPost);

			var penArea = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2);
			defPosLeft = penArea.intersectPerimeterPath(ballLeftPost).stream().findAny();
			defPosRight = penArea.intersectPerimeterPath(ballRightPost).stream().findAny();

		}

		if (defPosLeft.isEmpty() || defPosRight.isEmpty())
		{
			// This can only happen if the ball position is outside the field, or inside the penalty area
			return maxDefendersForBallThreat;
		}

		shapes.add(new DrawableLine(defPosLeft.get(), defPosRight.get(), Color.RED));

		var distance = defPosRight.get().distanceTo(defPosLeft.get());
		return (int) Math.ceil(distance / (2 * Geometry.getBotRadius()));
	}
}
