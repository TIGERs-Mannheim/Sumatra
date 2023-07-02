/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.List;


/**
 * Rate defense threats.
 */
public class DefenseThreatRater
{
	@Configurable(comment = "[%] Control how much of the danger distance is a DropOff", defValue = "0.25")
	private static double dangerDropOffPercentage = 0.25;

	@Configurable(comment = "[mm] A position further away than this + FieldLength / 2 is not dangerous at all", defValue = "1000.0")
	private static double dangerDistanceMinOffset = 1000.0;

	@Configurable(comment = "Weight to rate importance of the opponent redirect angle", defValue = "1.0")
	private static double redirectAngleWeight = 1.0;

	@Configurable(comment = "Weight to rate importance of the defense travel angle", defValue = "1.0")
	private static double travelAngleWeight = 1.0;

	@Configurable(comment = "Weight to rate importance of the opponent centralization", defValue = "1.0")
	private static double centralizedWeight = 1.0;
	@Configurable(comment = "Weight to rate importance of opponents being at the boarder", defValue = "0.5")
	private static double atBoarderWeight = 0.5;

	@Configurable(comment = "[deg] Maximum angle where a good redirect is expected, larger angles start to get lucky", defValue = "40.0")
	private static double maxGoodRedirectAngle = 40.0;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseThreatRater.class);
	}


	public static double getDangerZoneX()
	{
		return calcDangerDistanceMin() + Geometry.getGoalOur().getCenter().x();
	}


	private static double calcDangerDistanceMin()
	{
		return Geometry.getFieldLength() / 2 + dangerDistanceMinOffset;
	}


	public double getThreatRating(final IVector2 ballPos, final IVector2 threatPos)
	{
		var target = TriangleMath.bisector(threatPos, Geometry.getGoalOur().getLeftPost(),
				Geometry.getGoalOur().getRightPost());

		var scores = List.of(
				calcTravelAngleScore(ballPos, threatPos, target),
				calcRedirectAngleScore(ballPos, threatPos, target),
				calcCentralizedScore(threatPos, target),
				calcAtBoarderScore(threatPos)
		);
		double weightSum = scores.stream().mapToDouble(Score::weight).sum();
		double weightedScoreSum = scores.stream().mapToDouble(s -> s.score * s.weight).sum();

		return (weightedScoreSum / weightSum) * calcDistanceToGoalFactor(threatPos);
	}


	private double calcDistanceToGoalFactor(IVector2 threatPos)
	{
		var minDangerDistance = calcDangerDistanceMin();
		var minDangerDistanceBeforePenArea = minDangerDistance - Geometry.getPenaltyAreaDepth();
		var maxDangerDistance = minDangerDistanceBeforePenArea - minDangerDistanceBeforePenArea * dangerDropOffPercentage;


		// Closer positions are more dangerous
		double distToGoal = threatPos.distanceTo(Geometry.getGoalOur().getCenter());
		if (distToGoal < maxDangerDistance)
		{
			return 1.0;
		} else
		{
			double distDifference = minDangerDistance - maxDangerDistance;
			return 1 - (Math.min(distToGoal - maxDangerDistance, distDifference) / distDifference);
		}
	}


	private Score calcRedirectAngleScore(IVector2 ballPos, IVector2 threatPos, IVector2 target)
	{
		var opponentBall = Vector2.fromPoints(threatPos, ballPos);
		var opponentGoal = Vector2.fromPoints(threatPos, target);
		// Angle to redirect directly at the goal
		double volleyAngle = opponentBall.angleToAbs(opponentGoal).orElse(Math.PI);
		var maxAngle = AngleMath.deg2rad(maxGoodRedirectAngle);
		if (volleyAngle < maxAngle)
		{
			return new Score(1.0, redirectAngleWeight);
		} else
		{
			return new Score(Math.max(1 - (volleyAngle - maxAngle) / maxAngle, 0.0), redirectAngleWeight);
		}
	}


	private Score calcTravelAngleScore(IVector2 ballPos, IVector2 threatPos, IVector2 target)
	{
		var opponentGoal = Vector2.fromPoints(threatPos, target);
		var ballGoal = Vector2.fromPoints(ballPos, target);
		// How far has the keeper to travel to defend the new position
		return new Score(ballGoal.angleToAbs(opponentGoal).orElse(0.0) / Math.PI, travelAngleWeight);
	}


	private Score calcCentralizedScore(IVector2 threatPos, IVector2 target)
	{
		var opponentGoal = Vector2.fromPoints(threatPos, target);
		// Positions in the center are more dangerous
		return new Score((4.0 / 3.0) * Math.abs(opponentGoal.getAngle()) / Math.PI - (1.0 / 3.0), centralizedWeight);
	}


	private Score calcAtBoarderScore(IVector2 threatPos)
	{
		var distance = Geometry.getPenaltyAreaOur().withMargin(2 * Geometry.getBotRadius()).distanceTo(threatPos);
		if (distance <= 0)
		{
			return new Score(1, atBoarderWeight);
		} else
		{
			var score = SumatraMath.cap(1 - (distance / (3 * Geometry.getBotRadius())), 0, 1);
			return new Score(score, atBoarderWeight);
		}
	}


	private record Score(double score, double weight)
	{
	}
}
