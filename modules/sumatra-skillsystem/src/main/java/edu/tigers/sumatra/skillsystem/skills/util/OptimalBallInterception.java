/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


/**
 * Calculator class that determines the optimal time to intercept the ball.
 */
public final class OptimalBallInterception
{
	private static final Logger log = LogManager.getLogger(OptimalBallInterception.class.getName());

	private final IBallTrajectory ballTrajectory;
	private final ITrackedBot bot;
	private final IMoveConstraints mc;


	private OptimalBallInterception(final Builder builder)
	{
		ballTrajectory = builder.ballTrajectory;
		bot = builder.tBot;
		mc = builder.mc;
	}


	/**
	 * @return a new builder for this class
	 */
	public static Builder anOptimalBallInterceptor()
	{
		return new Builder();
	}


	/**
	 * @return the time in future when the ball can best be intercepted
	 */
	public double optimalInterceptionTime()
	{
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-4, 1e-2, new Checker());
		double tMax = Math.max(1e-5, ballTrajectory.getTimeByVel(0));
		tMax = withinFieldBoundaries(tMax);
		return multiStartOptimize(optimizer, tMax);
	}


	private double withinFieldBoundaries(final double tMax)
	{
		Optional<IVector2> fieldIntersection = ballTrajectory.getPlanarCurve()
				.getIntersectionsWithRectangle(Geometry.getField())
				.stream()
				.filter(p -> ballTrajectory.getTravelLineSegment().isPointOnLine(p))
				.findFirst();
		return fieldIntersection.map(pos -> Math.min(tMax, ballTrajectory.getTimeByPos(pos))).orElse(tMax);
	}


	private double multiStartOptimize(final UnivariateOptimizer optimizer, final double tMax)
	{
		Optional<UnivariatePointValuePair> result = optimize(optimizer, tMax, tMax / 2);
		if (result.isPresent())
		{
			if (result.get().getValue() > 1e-2)
			{
				double newTMax = result.get().getPoint();
				return optimize(optimizer, newTMax, newTMax / 2)
						.filter(p -> p.getValue() < 0.05)
						.map(UnivariatePointValuePair::getPoint)
						.orElse(tMax);
			}
			return result.get().getPoint();
		}
		return tMax;
	}


	private Optional<UnivariatePointValuePair> optimize(final UnivariateOptimizer optimizer, final double tMax,
			final double tStart)
	{
		try
		{
			UnivariatePointValuePair result = optimizer.optimize(
					GoalType.MINIMIZE,
					new MaxEval(20),
					new MaxIter(20),
					new SearchInterval(0, tMax, tStart),
					new UnivariateObjectiveFunction(this::objectiveFunction));
			return Optional.of(result);
		} catch (TooManyEvaluationsException | TooManyIterationsException e)
		{
			log.debug("Optimization failed.", e);
			return Optional.empty();
		}
	}


	private double objectiveFunction(final double t)
	{
		double shortTimeBonus = (1 - (t / Math.max(0.5, t))) * 0.1;
		return absSlackTime(t) - shortTimeBonus;
	}


	private double absSlackTime(final double t)
	{
		return Math.abs(slackTime(t));
	}


	/**
	 * @param t
	 * @return positive time if bot reaches the destination before the ball, negative else.
	 */
	public double slackTime(final double t)
	{
		IVector2 ballPos = ballTrajectory.getPosByTime(t).getXYVector();
		ITrajectory<IVector2> traj = getBotTrajectory(ballPos);
		return t - traj.getTotalTime();
	}


	private ITrajectory<IVector2> getBotTrajectory(final IVector2 ballPos)
	{
		// we assume here that the target orientation is equal to the current orientation for simplicity
		// the trajectory time will not differ much, when the robot is far away. And if it is nearby,
		// it should already have the target orientation
		// we would need to pass a function for calculating the target angle otherwise to generalize for receive and
		// redirect.
		IVector2 botDest = BotShape.getCenterFromKickerPos(ballPos, bot.getOrientation(), bot.getCenter2DribblerDist());
		return TrajectoryGenerator.generatePositionTrajectory(mc, bot.getPos(), bot.getVel(), botDest);
	}

	/**
	 * {@code BallInterceptor} builder static inner class.
	 */
	public static final class Builder
	{
		private IBallTrajectory ballTrajectory;
		private ITrackedBot tBot;
		private IMoveConstraints mc;


		private Builder()
		{
		}


		/**
		 * Sets the {@code ballTrajectory} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param ballTrajectory the {@code ballTrajectory} to set
		 * @return a reference to this Builder
		 */
		public Builder withBallTrajectory(final IBallTrajectory ballTrajectory)
		{
			this.ballTrajectory = ballTrajectory;
			return this;
		}


		/**
		 * Sets the {@code moveConstraints} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param mc the {@code moveConstraints} to set
		 * @return a reference to this Builder
		 */
		public Builder withMoveConstraints(final IMoveConstraints mc)
		{
			this.mc = mc;
			return this;
		}


		/**
		 * Set multiple fields based on the given tracked bots
		 *
		 * @param tBot a tracked bot
		 * @return a reference to this Builder
		 */
		public Builder withTrackedBot(final ITrackedBot tBot)
		{
			this.tBot = tBot;
			return this;
		}


		/**
		 * Returns a {@code BallInterceptor} built from the parameters previously set.
		 *
		 * @return a {@code BallInterceptor} built with parameters of this {@code BallInterceptor.Builder}
		 */
		public OptimalBallInterception build()
		{
			Validate.notNull(ballTrajectory);
			Validate.notNull(tBot);
			Validate.notNull(mc);
			return new OptimalBallInterception(this);
		}
	}

	private static class Checker implements ConvergenceChecker<UnivariatePointValuePair>
	{
		@Override
		public boolean converged(final int iteration, final UnivariatePointValuePair previous,
				final UnivariatePointValuePair current)
		{
			return false;
		}
	}
}
