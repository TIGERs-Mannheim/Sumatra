/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.validators;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Check if all balls are in line with the robots orientation.
 *
 * @author AndreR
 */
public class DirectionValidator implements IKickValidator
{
	@Configurable(defValue = "45.0", comment = "Maximum angle difference for a valid kick")
	private static double maxAngleDiff = 45.0;

	private double lastAngDiffDeg = 0;

	static
	{
		ConfigRegistration.registerClass("vision", DirectionValidator.class);
	}


	@Override
	public String getName()
	{
		return String.format("Dir (%5.1f)", lastAngDiffDeg);
	}


	@Override
	public boolean validateKick(final List<FilteredVisionBot> bots, final List<MergedBall> balls)
	{
		FilteredVisionBot bot = bots.get(0);

		Map<Integer, List<MergedBall>> groupedBalls = balls.stream()
				.collect(Collectors.groupingBy((final MergedBall b) -> b.getLatestCamBall().get().getCameraId()));

		for (List<MergedBall> group : groupedBalls.values())
		{
			List<IVector2> ballPos = group.stream()
					.map(b -> b.getLatestCamBall().get().getPos().getXYVector())
					.collect(Collectors.toList());

			Optional<Line> line = Line.fromPointsList(ballPos);
			if (!line.isPresent() || (group.size() < 3))
			{
				continue;
			}

			Optional<Double> angDiff = line.get().directionVector().angleToAbs(Vector2.fromAngle(bot.getOrientation()));

			if (angDiff.isPresent())
			{
				lastAngDiffDeg = AngleMath.rad2deg(angDiff.get());

				if (angDiff.get() < AngleMath.deg2rad(maxAngleDiff))
				{
					return true;
				}
			}
		}

		return false;
	}


	/**
	 * Backrack a list of merged balls to the kicking robot.
	 *
	 * @param bots
	 * @param balls
	 * @return Pair of kick timestamp and kick position.
	 */
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public static Optional<Pair<Long, IVector2>> backtrack(final List<FilteredVisionBot> bots,
			final List<MergedBall> balls)
	{
		FilteredVisionBot bot = bots.get(0);

		Map<Integer, List<MergedBall>> groupedBalls = balls.stream()
				.collect(Collectors.groupingBy((final MergedBall b) -> b.getLatestCamBall().get().getCameraId()));

		double smallestAngDiff = Math.PI;
		Optional<IVector2> kickPos = Optional.empty();
		List<MergedBall> bestGroup = null;

		for (List<MergedBall> group : groupedBalls.values())
		{
			if (group.size() < 3)
			{
				continue;
			}

			List<IVector2> ballPos = group.stream()
					.map(b -> b.getLatestCamBall().get().getPos().getXYVector())
					.collect(Collectors.toList());

			Optional<Line> line = Line.fromPointsList(ballPos);
			if (!line.isPresent())
			{
				return Optional.empty();
			}

			Optional<Double> angDiff = line.get().directionVector().angleToAbs(Vector2.fromAngle(bot.getOrientation()));

			if (angDiff.isPresent() && (angDiff.get() < smallestAngDiff))
			{
				smallestAngDiff = angDiff.get();

				Vector2 kickerCenter = Vector2.fromAngle(bot.getOrientation()).multiply(105).add(bot.getPos());
				Line front = Line.fromDirection(kickerCenter, Vector2.fromAngle(bot.getOrientation() + AngleMath.PI_HALF));

				kickPos = front.intersectionWith(line.get());
				bestGroup = group;
			}
		}

		if (bestGroup != null)
		{
			int numPoints = bestGroup.size();

			RealMatrix matA = new Array2DRowRealMatrix(numPoints, 2);
			RealVector b = new ArrayRealVector(numPoints);

			CamBall bFirst = bestGroup.get(0).getLatestCamBall().get();
			for (int i = 0; i < numPoints; i++)
			{
				matA.setEntry(i, 0,
						bestGroup.get(i).getLatestCamBall().get().getPos().getXYVector().distanceTo(kickPos.get()));
				matA.setEntry(i, 1, 1.0);

				CamBall bNow = bestGroup.get(i).getLatestCamBall().get();
				double time = ((bNow.gettCapture()) - bFirst.gettCapture()) * 1e-9;
				b.setEntry(i, time);
			}

			DecompositionSolver solver = new QRDecomposition(matA).getSolver();
			RealVector x;
			try
			{
				x = solver.solve(b);
			} catch (SingularMatrixException e)
			{
				return Optional.empty();
			}

			long tKick = ((long) (x.getEntry(1) * 1e9)) + bFirst.gettCapture();

			return Optional.of(new Pair<>(tKick, kickPos.get()));
		}

		return Optional.empty();
	}
}
