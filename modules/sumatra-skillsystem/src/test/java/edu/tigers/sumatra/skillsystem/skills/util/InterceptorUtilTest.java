/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;
import org.junit.Test;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InterceptorUtilTest
{
	
	private Random rnd = new Random(42);
	
	
	@Test
	public void testFastestPointOnLine() throws Exception
	{
		IntStream.rangeClosed(0, 500)
				.forEach(this::doTestFastestPointOnLine);
	}
	
	
	private void doTestFastestPointOnLine(final int i)
	{
		IVector2 p1 = randomPointOnField();
		IVector2 p2 = randomPointOnField();
		ILineSegment line = Lines.segmentFromPoints(p1, p2);
		
		IVector2 botPos = Vector2.zero();
		IVector2 botVel = Vector2.fromAngle(rnd.nextDouble() * AngleMath.PI_TWO).scaleTo(rnd.nextDouble() * 2);
		
		MoveConstraints moveConstraints = new MoveConstraints(new BotMovementLimits());
		
		IVector2 bestExpected = naiive(line, botPos, botVel, moveConstraints);
		Range range = findRange(line, botPos, botVel, moveConstraints, bestExpected);
		
		IVector2 bestActual = InterceptorUtil.fastestPointOnLine(line, botPos, botVel, moveConstraints).getTarget();
		double distActual = line.getStart().distanceTo(bestActual);
		
		// should be at least within 5cm. The precision of the trajectories is not that high...
		assertThat(range.containsDouble(distActual))
				.as("i=" + i + " range=" + range + " distActual=" + distActual)
				.isTrue();
	}
	
	
	private IVector2 naiive(final ILineSegment line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints)
	{
		double bestTime = Double.MAX_VALUE;
		IVector2 bestDest = null;
		for (double d = 0; d < line.getDisplacement().getLength2(); d += 1)
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
	
	
	private Range findRange(final ILineSegment line, final IVector2 botPos, final IVector2 botVel,
			final MoveConstraints moveConstraints, final IVector2 bestDest)
	{
		double startDist = line.getStart().distanceTo(bestDest);
		double startTime = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, bestDest)
				.getTotalTime();
		
		double min = 0;
		for (double dist = startDist; dist >= 0; dist -= 1)
		{
			IVector2 dest = line.getStart().addNew(line.directionVector().scaleToNew(dist));
			double time = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, dest)
					.getTotalTime();
			if (Math.abs(time - startTime) > 0.05)
			{
				break;
			}
			min = line.getStart().distanceTo(dest);
		}
		
		double max = 0;
		for (double dist = startDist; dist < line.getDisplacement().getLength2(); dist += 1)
		{
			IVector2 dest = line.getStart().addNew(line.directionVector().scaleToNew(dist));
			double time = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, dest)
					.getTotalTime();
			if (Math.abs(time - startTime) > 0.05)
			{
				break;
			}
			max = line.getStart().distanceTo(dest);
		}
		
		double margin = 1;
		return new DoubleRange(min - margin, max + margin);
	}
	
	
	private IVector2 randomPointOnField()
	{
		return Vector2.fromXY((rnd.nextDouble() - 0.5) * Geometry.getFieldLength(),
				(rnd.nextDouble() - 0.5) * Geometry.getFieldWidth());
	}
}