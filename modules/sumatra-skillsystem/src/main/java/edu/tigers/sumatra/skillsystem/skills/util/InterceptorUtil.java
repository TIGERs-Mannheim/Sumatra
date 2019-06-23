/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import static java.lang.Math.abs;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Utility methods for interceptions
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class InterceptorUtil
{
	private InterceptorUtil()
	{
	}
	
	
	/**
	 * Find the point on the given line where the robot can move to fastest
	 *
	 * @param line a line to move to
	 * @param bot the bot to use
	 * @param moveConstraints the moveConstraints for generating trajectories
	 * @return a destination on the line
	 */
	public static TemporalTarget fastestPointOnLine(ILineSegment line, ITrackedBot bot, MoveConstraints moveConstraints)
	{
		return fastestPointOnLine(line, bot.getPos(), bot.getVel(), moveConstraints);
	}
	
	
	/**
	 * Find the point on the given line where the robot can move to fastest
	 *
	 * @param line a line to move to
	 * @param botPos the bot position
	 * @param botVel the bot velocity
	 * @param moveConstraints the moveConstraints for generating trajectories
	 * @return a destination on the line
	 */
	public static TemporalTarget fastestPointOnLine(ILineSegment line, IVector2 botPos, IVector2 botVel,
			MoveConstraints moveConstraints)
	{
		return brentOptimizer(line, botPos, botVel, moveConstraints);
	}
	
	
	private static TemporalTarget brentOptimizer(final ILineSegment line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints)
	{
		UnivariateOptimizer optimizer = new BrentOptimizer(0.0001, 1, new Checker());
		UnivariateFunction func = dist -> time4Dist(line, botPos, botVel, moveConstraints, dist);
		double dMax = line.getDisplacement().getLength2();
		
		if (dMax < 1)
		{
			// optimizer does not like zero length interval
			return new TemporalTarget(line.getStart(), func.value(0));
		}
		
		UnivariatePointValuePair result = optimizer.optimize(
				GoalType.MINIMIZE,
				new MaxEval(100),
				new MaxIter(100),
				new SearchInterval(0, dMax, dMax / 2),
				new UnivariateObjectiveFunction(func));
		
		return new TemporalTarget(dest4Dist(line, result.getPoint()), result.getValue());
	}
	
	
	private static double time4Dist(
			final ILineSegment line,
			final IVector2 botPos,
			final IVector2 botVel,
			final MoveConstraints moveConstraints,
			final double dist)
	{
		IVector2 dest = dest4Dist(line, dist);
		ITrajectory<IVector2> trajectory = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel,
				dest);
		return trajectory.getTotalTime();
	}
	
	
	private static IVector2 dest4Dist(final ILineSegment line, final double dist)
	{
		return line.getStart().addNew(line.directionVector().scaleToNew(dist));
	}
	
	private static class Checker implements ConvergenceChecker<UnivariatePointValuePair>
	{
		@Override
		public boolean converged(final int iteration, final UnivariatePointValuePair previous,
				final UnivariatePointValuePair current)
		{
			return abs(previous.getPoint() - current.getPoint()) < 1;
		}
	}
	
	/**
	 * A target with time
	 */
	public static class TemporalTarget
	{
		private final IVector2 target;
		private final double time;
		
		
		/**
		 * New target
		 * 
		 * @param target
		 * @param time
		 */
		public TemporalTarget(final IVector2 target, final double time)
		{
			this.target = target;
			this.time = time;
		}
		
		
		public IVector2 getTarget()
		{
			return target;
		}
		
		
		public double getTime()
		{
			return time;
		}
	}
	
}
