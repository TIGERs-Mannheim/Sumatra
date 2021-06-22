/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.RatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;


/**
 * A rater that rates how well a goal could be scored from the pass target.
 * It does not respect the pass in contrast to {@link ReflectorRater}.
 */
public class GoalRater implements IPassRater
{
	private final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
	private final Collection<ITrackedBot> obstacles;


	public GoalRater(Collection<ITrackedBot> obstacles)
	{
		this.obstacles = obstacles;
		rater.setObstacles(obstacles);
	}


	@Override
	public double rate(Pass pass)
	{
		return rateGoalKick(pass).getScore();
	}


	private IRatedTarget rateGoalKick(Pass pass)
	{
		rater.setObstacles(obstacles);
		// For this rater, we assume that the ball is already at the target
		rater.setTimeToKick(0);
		return rater.rate(pass.getKick().getTarget())
				.orElseGet(() -> RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));
	}
}
