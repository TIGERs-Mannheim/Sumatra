/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import static edu.tigers.sumatra.math.SumatraMath.sqrt;
import static java.lang.Math.abs;

import java.util.Random;

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
import org.junit.Test;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InterceptionCalcPerfTest
{
	private Random rnd = new Random(42);
	
	
	@Test
	public void testLineInterception()
	{
		double diffBinarySum = 0;
		double diffBrentSum = 0;
		double diffGoldenSectionSum = 0;
		long tNaiiveSum = 0;
		long tBinarySum = 0;
		long tBrentSum = 0;
		long tGoldenSectionSum = 0;
		int n = 5000;
		for (int i = 0; i < n; i++)
		{
			IVector2 p1 = randomPointOnField();
			IVector2 p2 = randomPointOnField();
			ILine line = Line.fromPoints(p1, p2);
			
			IVector2 botPos = randomPointOnField();
			IVector2 botVel = Vector2.fromAngle(rnd.nextDouble() * AngleMath.PI_TWO).scaleTo(rnd.nextDouble() * 2);
			
			MoveConstraints moveConstraints = new MoveConstraints(new BotMovementLimits());
			
			long t0 = System.nanoTime();
			IVector2 bestNaiive = naiive(line, botPos, botVel, moveConstraints);
			long t1 = System.nanoTime();
			IVector2 bestBinary = binary(line, botPos, botVel, moveConstraints);
			long t2 = System.nanoTime();
			IVector2 bestBrent = brent(line, botPos, botVel, moveConstraints);
			long t3 = System.nanoTime();
			IVector2 bestGoldenSection = goldenSection(line, botPos, botVel, moveConstraints);
			long t4 = System.nanoTime();
			
			long tNaiive = (t1 - t0) / 1000;
			tNaiiveSum += tNaiive;
			long tBinary = (t2 - t1) / 1000;
			tBinarySum += tBinary;
			long tBrent = (t3 - t2) / 1000;
			tBrentSum += tBrent;
			long tGoldenSection = (t4 - t3) / 1000;
			tGoldenSectionSum += tGoldenSection;
			
			double diffBinary = bestNaiive.distanceTo(bestBinary);
			diffBinarySum += diffBinary;
			double diffBrent = bestNaiive.distanceTo(bestBrent);
			diffBrentSum += diffBrent;
			double diffGoldenSection = bestNaiive.distanceTo(bestGoldenSection);
			diffGoldenSectionSum += diffGoldenSection;
		}
		System.out.println("avgDiffBinary: " + (diffBinarySum / n));
		System.out.println(" avgDiffBrent: " + (diffBrentSum / n));
		System.out.println(" avgDiffGoSec: " + (diffGoldenSectionSum / n));
		System.out.println();
		System.out.println("tNaiive: " + (tNaiiveSum / n));
		System.out.println("tBinary: " + (tBinarySum / n));
		System.out.println(" tBrent: " + (tBrentSum / n));
		System.out.println(" tGoSec: " + (tGoldenSectionSum / n));
	}
	
	
	private IVector2 naiive(final ILine line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints)
	{
		double bestTime = Double.MAX_VALUE;
		IVector2 bestDest = null;
		for (double d = 0; d < line.directionVector().getLength2(); d += 1)
		{
			IVector2 dest = line.getStart().addNew(line.directionVector().scaleToNew(d));
			double time = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, dest)
					.getTotalTime();
			if (time < bestTime)
			{
				bestTime = time;
				bestDest = dest;
			}
		}
		return bestDest;
	}
	
	
	private IVector2 binary(final ILine line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints)
	{
		double d1 = 0;
		double d2 = line.directionVector().getLength2();
		
		double t1 = time4Dist(line, botPos, botVel, moveConstraints, d1);
		double t2 = time4Dist(line, botPos, botVel, moveConstraints, d2);
		while ((d2 - d1) > 1)
		{
			if (t1 > t2)
			{
				d1 += (d2 - d1) / 2.0;
				t1 = time4Dist(line, botPos, botVel, moveConstraints, d1);
			} else
			{
				d2 -= (d2 - d1) / 2.0;
				t2 = time4Dist(line, botPos, botVel, moveConstraints, d2);
			}
			if (d1 > d2)
			{
				throw new IllegalStateException();
			}
		}
		return line.getStart().addNew(line.directionVector().scaleToNew(d1));
	}
	
	
	private IVector2 brent(final ILine line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints)
	{
		UnivariateOptimizer optimizer = new BrentOptimizer(0.0001, 1, new Checker());
		UnivariateFunction func = (dist) -> time4Dist(line, botPos, botVel, moveConstraints, dist);
		double dMax = line.directionVector().getLength2();
		
		UnivariatePointValuePair result = optimizer.optimize(
				GoalType.MINIMIZE,
				new MaxEval(100),
				new MaxIter(100),
				new SearchInterval(0, dMax, dMax / 2),
				new UnivariateObjectiveFunction(func));
		
		double d1 = result.getPoint();
		
		return line.getStart().addNew(line.directionVector().scaleToNew(d1));
	}
	
	private static final double GOLDEN_RATIO = (sqrt(5.0) + 1.0) / 2.0;
	
	
	private IVector2 goldenSection(final ILine line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints)
	{
		double a = 0;
		double b = line.directionVector().getLength2();
		
		double c = b - ((b - a) / GOLDEN_RATIO);
		double d = a + ((b - a) / GOLDEN_RATIO);
		
		while (abs(c - d) > 1)
		{
			double tc = time4Dist(line, botPos, botVel, moveConstraints, c);
			double td = time4Dist(line, botPos, botVel, moveConstraints, d);
			if (tc < td)
			{
				b = d;
			} else
			{
				a = c;
			}
			c = b - ((b - a) / GOLDEN_RATIO);
			d = a + ((b - a) / GOLDEN_RATIO);
		}
		
		double dist = (b + a) / 2.0;
		return line.getStart().addNew(line.directionVector().scaleToNew(dist));
	}
	
	
	private double time4Dist(
			final ILine line,
			final IVector2 botPos, final IVector2 botVel, final MoveConstraints moveConstraints, final double d)
	{
		IVector2 dest = line.getStart().addNew(line.directionVector().scaleToNew(d));
		return TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, dest)
				.getTotalTime();
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
	
	
	private IVector2 randomPointOnField()
	{
		return Vector2.fromXY((rnd.nextDouble() * Geometry.getFieldLength()) - (Geometry.getFieldLength() / 2),
				(rnd.nextDouble() * Geometry.getFieldWidth()) - (Geometry.getFieldWidth() / 2));
	}
}
