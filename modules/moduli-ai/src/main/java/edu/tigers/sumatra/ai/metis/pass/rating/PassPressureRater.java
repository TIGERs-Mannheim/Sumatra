/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Rate passes to the left or right of the goal better than towards our goal or the opponent goal center or field border.
 * This way, a "pressure" towards to opponent goal is modeled.
 */
public class PassPressureRater implements IPassRater
{
	@Configurable(defValue = "-0.5")
	private static double minXRatingPosRel = -0.5;

	static
	{
		ConfigRegistration.registerClass("metis", RatedPassFactory.class);
	}

	@Override
	public double rate(Pass pass)
	{
		return pressure(pass.getKick().getTarget());
	}


	private double pressure(IVector2 target)
	{
		double maxXRatingPos = Geometry.getFieldLength() / 2.0;
		double minXRatingPos = maxXRatingPos * minXRatingPosRel;
		double xRating = SumatraMath.relative(target.x(), minXRatingPos, maxXRatingPos);

		double yGoalCornerMid = Geometry.getFieldWidth() / 4.0;
		double yDistToGoalCornerMid = Math.abs(Math.abs(target.y()) - yGoalCornerMid);

		double distToOptimalYPosRating = SumatraMath.relative(yDistToGoalCornerMid, 0,
				Geometry.getFieldWidth() / 4.0);
		double yRating = 1 - distToOptimalYPosRating;

		return xRating * Math.max(0.1, yRating);
	}
}
