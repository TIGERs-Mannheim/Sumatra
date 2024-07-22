/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.NonNull;


/**
 * Rate passes to the left or right of the goal better than towards our goal or the opponent goal center or field border.
 * This way, a "pressure" towards to opponent goal is modeled.
 */
public class PassPressureRater implements IPassRater
{
	@Override
	public double rate(Pass pass)
	{
		return pressure(pass.getKick().getTarget(), pass.getKick().getSource());
	}


	private double pressure(IVector2 target, @NonNull IVector2 source)
	{
		double len = Geometry.getFieldLength() / 2.0;
		double maxXRatingPos = len * 0.75;
		double minXRatingPos = maxXRatingPos * Math.min(source.x() / len - 0.35, 0.25);
		double xRating = SumatraMath.relative(target.x(), minXRatingPos, maxXRatingPos);

		double yGoalCornerMid = Geometry.getFieldWidth() / 6.0;
		double yDistToGoalCornerMid = Math.abs(Math.abs(target.y()) - yGoalCornerMid);

		double distToOptimalYPosRating = SumatraMath.relative(yDistToGoalCornerMid, 0,
				Geometry.getFieldWidth() / 2.5);
		double yRating = 1 - distToOptimalYPosRating;

		return Math.min(1.0, xRating * Math.max(0.1, yRating) + (Math.max(0, target.x() * 0.5)) / len);
	}
}
