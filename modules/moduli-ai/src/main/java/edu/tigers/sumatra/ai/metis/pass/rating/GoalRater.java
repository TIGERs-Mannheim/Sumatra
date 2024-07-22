/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.RatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;


/**
 * A rater that rates how well a goal could be scored from the pass target.
 * It does not respect redirect goal kicks in contrast to {@link ReflectorRater}.
 */
public class GoalRater implements IPassRater
{
	@Configurable(defValue = "0.5")
	private static double bestImprovement = 0.5;

	static
	{
		ConfigRegistration.registerClass("metis", GoalRater.class);
	}

	private final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());


	public GoalRater(Collection<ITrackedBot> obstacles)
	{
		rater.setObstacles(obstacles);
	}


	@Override
	public double rate(Pass pass)
	{
		var ratedSource = rater.rate(pass.getKick().getSource())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));
		var ratedTarget = rater.rate(pass.getKick().getTarget())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));

		var improvement = ratedTarget.getScore() - ratedSource.getScore();
		return Math.min(SumatraMath.relative(improvement, 0, bestImprovement), ratedTarget.getScore());
	}
}
