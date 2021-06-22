/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.RatedTarget;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;
import java.util.List;


/**
 * A nice rater that rates a pass by the ability to score
 * a goal from the pass target, respecting the redirect angle.
 */
public class ReflectorRater implements IPassRater
{
	@Configurable(defValue = "0.9")
	private static double minCriticalRedirectAngle = 0.9;

	@Configurable(defValue = "1.2")
	private static double maxCriticalRedirectAngle = 1.2;

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
		IRatedTarget ratedTarget = rateGoalKick(pass);
		var angle = OffensiveMath.getRedirectAngle(
				pass.getKick().getSource(),
				pass.getKick().getTarget(),
				ratedTarget.getTarget());
		double reflectScore = 1 - SumatraMath.relative(angle, minCriticalRedirectAngle, maxCriticalRedirectAngle);
		double goalKickScore = ratedTarget.getScore();
		return reflectScore * goalKickScore;
	}


	private IRatedTarget rateGoalKick(Pass pass)
	{
		rater.setObstacles(obstacles);
		return rater.rate(pass.getKick().getTarget())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));
	}


	@Override
	public List<IDrawableShape> createDebugShapes()
	{
		return rater.createDebugShapes();
	}
}
