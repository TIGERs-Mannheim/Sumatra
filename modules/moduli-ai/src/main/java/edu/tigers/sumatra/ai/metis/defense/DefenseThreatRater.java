package edu.tigers.sumatra.ai.metis.defense;

import org.apache.commons.math3.distribution.BetaDistribution;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Rate defense threats.
 */
public class DefenseThreatRater
{
	@Configurable(comment = "maximum x value for a foe bot to 'be in our half' (mm)", defValue = "50.0")
	private static double dangerZoneX = 50.0;

	@Configurable(comment = "ER-Force volley shot threat weight", defValue = "5.0")
	private static double volleyAngleWeight = 5.0;

	@Configurable(comment = "ER-Force travel angle threat weight", defValue = "1.0")
	private static double travelAngleWeight = 1.0;

	@Configurable(comment = "ER-Force distance to goal weight", defValue = "1.0")
	private static double distToGoalWeight = 1.0;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseThreatRater.class);
	}


	public double getThreatRating(final IVector2 threatPos, final IVector2 botPos)
	{
		IVector2 target = Geometry.getGoalOur().getCenter();
		BetaDistribution beta = new BetaDistribution(4, 6);

		double distGoal = dangerZoneX - Geometry.getGoalOur().getCenter().x();
		double distanceToGoal = 1.0 - (Math.min(botPos.distanceTo(target), distGoal) / distGoal);

		IVector2 ballOpponent = Vector2.fromPoints(botPos, threatPos);
		IVector2 opponentGoal = Vector2.fromPoints(botPos, target);
		IVector2 ballGoal = Vector2.fromPoints(threatPos, target);
		IVector2 goalOpponent = Vector2.fromPoints(target, opponentGoal);

		double volleyAngle = ballOpponent.angleToAbs(opponentGoal).orElse(0.0) / Math.PI;
		volleyAngle = beta.density(volleyAngle) / 2.5; // scale to 1.0
		double travelAngle = ballGoal.angleToAbs(goalOpponent).orElse(0.0) / Math.PI;

		return ((volleyAngle * volleyAngleWeight) + (travelAngle * travelAngleWeight)
				+ (distanceToGoal * distToGoalWeight)) /
				(volleyAngleWeight + travelAngleWeight + distToGoalWeight);
	}


	public static double getDangerZoneX()
	{
		return dangerZoneX;
	}
}
