/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * Rate passes to the left or right of the goal better than towards our goal or the opponent goal center or field border.
 * This way, a "pressure" towards to opponent goal is modeled.
 */
public class PassPressureRater implements IPassRater
{
	@Configurable(defValue = "-3000.0")
	private static double minXRatingPos = -3000.0;

	static
	{
		ConfigRegistration.registerClass("metis", RatedPassFactory.class);
	}

	@Override
	public double rate(Pass pass)
	{
		var target = pass.getKick().getTarget();
		double maxXRatingPos = Geometry.getFieldLength() / 2.0;
		double xRating = SumatraMath.relative(target.x(), minXRatingPos, maxXRatingPos);

		double yGoalCornerMid = Geometry.getFieldWidth() / 4.0;
		double yDistToGoalCornerMid = Math.abs(Math.abs(target.y()) - yGoalCornerMid);

		double distToOptimalYPosRating = SumatraMath.relative(yDistToGoalCornerMid, 0,
				Geometry.getFieldWidth() / 4.0);
		double yRating = 1 - distToOptimalYPosRating;

		return xRating * Math.max(0.1, yRating);
	}
}
