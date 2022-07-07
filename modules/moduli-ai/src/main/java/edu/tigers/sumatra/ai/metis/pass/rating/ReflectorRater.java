/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.RatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;

import static edu.tigers.sumatra.math.SumatraMath.relative;


/**
 * A nice rater that rates a pass by the ability to score
 * a goal from the pass target, respecting the redirect angle.
 */
public class ReflectorRater extends APassRater
{
	@Configurable(defValue = "0.9")
	private static double minCriticalAngle = 0.9;

	@Configurable(defValue = "1.2")
	private static double maxCriticalAngle = 1.2;

	@Configurable(defValue = "0.5")
	private static double bestImprovement = 0.5;

	static
	{
		ConfigRegistration.registerClass("metis", ReflectorRater.class);
	}

	private final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
	private final Collection<ITrackedBot> obstacles;


	public ReflectorRater(Collection<ITrackedBot> obstacles)
	{
		this.obstacles = obstacles;
		rater.setObstacles(obstacles);
	}


	@Override
	public double rate(Pass pass)
	{
		rater.setObstacles(obstacles);
		var ratedTarget = rater.rate(pass.getKick().getTarget())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));
		double goalKickScore = rateGoalKick(pass, ratedTarget);

		var angle = OffensiveMath.getRedirectAngle(
				pass.getKick().getSource(),
				pass.getKick().getTarget(),
				ratedTarget.getTarget());
		double reflectScore = 1 - relative(angle, minCriticalAngle, maxCriticalAngle);
		return reflectScore * goalKickScore;
	}


	private double rateGoalKick(Pass pass, IRatedTarget ratedTarget)
	{
		var ratedSource = rater.rate(pass.getKick().getSource())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));

		var improvement = ratedTarget.getScore() - ratedSource.getScore();
		return relative(improvement, 0, bestImprovement);
	}
}
