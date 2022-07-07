/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.apache.commons.math3.distribution.BetaDistribution;


/**
 * Rate defense threats.
 */
public class DefenseThreatRater
{
	@Configurable(comment = "[%] Control how much of the danger distance is a DropOff", defValue = "0.15")
	private static double dangerDropOffPercentage = 0.15;

	@Configurable(comment = "[mm] A position further away than this + FieldLength / 2 is not dangerous at all", defValue = "1000.0")
	private static double dangerDistanceMinOffset = 1000.0;

	@Configurable(comment = "ER-Force volley shot threat weight", defValue = "5.0")
	private static double volleyAngleWeight = 5.0;

	@Configurable(comment = "ER-Force travel angle threat weight", defValue = "1.0")
	private static double travelAngleWeight = 1.0;

	@Configurable(comment = "ER-Force distance to goal weight", defValue = "1.0")
	private static double distToGoalWeight = 1.0;

	@Configurable(comment = "ER-Force volley shot threat weight", defValue = "1.0")
	private static double altVolleyAngleWeight = 1.0;

	@Configurable(comment = "ER-Force travel angle threat weight", defValue = "1.0")
	private static double altTravelAngleWeight = 1.0;

	@Configurable(comment = "ER-Force distance to goal weight", defValue = "1.0")
	private static double altCentralizedWeight = 1.0;

	@Configurable(comment = "[deg] Maximum angle where a good redirect is expected, larger angles start to get lucky", defValue = "40.0")
	private static double maxGoodRedirectAngle = 40.0;

	@Configurable(defValue = "ALTERNATIVE")
	private static ECalculationMethod calculationMethod = ECalculationMethod.ALTERNATIVE;

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
		return switch (calculationMethod)
				{
					case LEGACY -> getThreatRatingLegacy(ballPos, threatPos);
					case ALTERNATIVE -> getThreatRatingAlternative(ballPos, threatPos);
				};
	}


	private double getThreatRatingLegacy(final IVector2 ballPos, final IVector2 threatPos)
	{
		var minDangerDistance = calcDangerDistanceMin();

		IVector2 target = Geometry.getGoalOur().getCenter();
		BetaDistribution beta = new BetaDistribution(4, 6);

		final double distanceToGoal =
				1.0 - (Math.min(threatPos.distanceTo(target), minDangerDistance) / minDangerDistance);

		IVector2 opponentBall = Vector2.fromPoints(threatPos, ballPos);
		IVector2 opponentGoal = Vector2.fromPoints(threatPos, target);
		IVector2 ballGoal = Vector2.fromPoints(ballPos, target);

		final double volleyAngle = beta.density(opponentBall.angleToAbs(opponentGoal).orElse(0.0) / Math.PI) / 2.5;
		final double travelAngle = ballGoal.angleToAbs(opponentGoal).orElse(0.0) / Math.PI;
		return ((volleyAngle * volleyAngleWeight) + (travelAngle * travelAngleWeight)
				+ (distanceToGoal * distToGoalWeight)) /
				(volleyAngleWeight + travelAngleWeight + distToGoalWeight);
	}


	private double getThreatRatingAlternative(final IVector2 ballPos, final IVector2 threatPos)
	{
		var minDangerDistance = calcDangerDistanceMin();
		var minDangerDistanceBeforePenArea = minDangerDistance - Geometry.getPenaltyAreaDepth();
		var maxDangerDistance = minDangerDistanceBeforePenArea - minDangerDistanceBeforePenArea * dangerDropOffPercentage;

		final IVector2 target = Geometry.getGoalOur().getCenter();

		// Closer positions are more dangerous
		final double distToGoal = threatPos.distanceTo(target);
		final double distanceToGoalFactor;
		if (distToGoal < maxDangerDistance)
		{
			distanceToGoalFactor = 1.0;
		} else
		{
			final double distDifference = minDangerDistance - maxDangerDistance;
			distanceToGoalFactor = 1 - (Math.min(distToGoal - maxDangerDistance, distDifference) / distDifference);
		}

		final IVector2 opponentBall = Vector2.fromPoints(threatPos, ballPos);
		final IVector2 opponentGoal = Vector2.fromPoints(threatPos, target);
		IVector2 ballGoal = Vector2.fromPoints(ballPos, target);

		// How far has the keeper to travel to defend the new position
		final double travelAngle = ballGoal.angleToAbs(opponentGoal).orElse(0.0) / Math.PI;

		// Angle to redirect directly at the goal
		final double volleyAngle = opponentBall.angleToAbs(opponentGoal).orElse(Math.PI);
		final double maxAngle = AngleMath.deg2rad(maxGoodRedirectAngle);
		final double volleyScore;
		if (volleyAngle < maxAngle)
		{
			volleyScore = 1.0;
		} else
		{
			volleyScore = Math.max(1 - (volleyAngle - maxAngle) / maxAngle, 0.0);
		}

		// Positions in the center are more dangerous
		final double centralizedScore = (4.0 / 3.0) * Math.abs(opponentGoal.getAngle()) / Math.PI - (1.0 / 3.0);

		return ((volleyScore * altVolleyAngleWeight) + (travelAngle * altTravelAngleWeight) + (centralizedScore
				* altCentralizedWeight)) / (altVolleyAngleWeight + altTravelAngleWeight + altCentralizedWeight)
				* distanceToGoalFactor;
	}


	private enum ECalculationMethod
	{
		LEGACY,
		ALTERNATIVE,
	}
}
