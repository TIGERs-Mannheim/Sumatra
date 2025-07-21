/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.straight;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.kick.estimators.IKickSolver;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import java.util.List;
import java.util.Optional;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class StraightKickSolverNonLin3Direct implements IKickSolver
{
	private final SimplexOptimizer optimizer = new SimplexOptimizer(1e-3, 1e-3);
	private final NelderMeadSimplex simplex = new NelderMeadSimplex(3, 10.0);
	private double[] initialGuess = new double[3];


	/**
	 * @param kickPosition
	 * @param kickVelocity
	 */
	public StraightKickSolverNonLin3Direct(final IVector2 kickPosition, final double kickVelocity)
	{
		initialGuess[0] = kickPosition.x();
		initialGuess[1] = kickPosition.y();
		initialGuess[2] = kickVelocity;
	}


	@Override
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<KickSolverResult> solve(final List<CamBall> records)
	{
		final long tZero = records.get(0).getTimestamp();

		List<IVector2> groundPos = records.stream()
				.map(CamBall::getFlatPos)
				.toList();

		Optional<ILineSegment> kickLine = Lines.regressionLineFromPointsList(groundPos);
		if (kickLine.isEmpty())
		{
			return Optional.empty();
		}

		IVector2 dir = kickLine.get().directionVector().normalizeNew();

		double[] result;
		try
		{
			final PointValuePair optimum = optimizer.optimize(
					new MaxEval(50),
					new ObjectiveFunction(new StraightBallModel(records, dir)),
					GoalType.MINIMIZE,
					new InitialGuess(initialGuess),
					simplex);

			result = optimum.getPoint();
		} catch (IllegalStateException e)
		{
			// compute the current simplex center => best estimate
			double[] sum = new double[] { 0, 0, 0 };
			PointValuePair[] points = simplex.getPoints();

			for (PointValuePair pair : points)
			{
				for (int i = 0; i < 3; i++)
				{
					sum[i] += pair.getPointRef()[i];
				}
			}

			for (int i = 0; i < 3; i++)
			{
				sum[i] /= points.length;
			}

			result = sum;
		}

		initialGuess = result;

		IVector2 kickPos = Vector2.fromXY(result[0], result[1]);
		IVector3 kickVel = dir.scaleToNew(result[2]).getXYZVector();

		return Optional.of(new KickSolverResult(kickPos, kickVel, tZero, getClass().getSimpleName()));
	}


	private static class StraightBallModel implements MultivariateFunction
	{
		private final List<CamBall> records;
		private final IVector2 kickDir;


		/**
		 * @param records
		 * @param kickDir
		 */
		public StraightBallModel(final List<CamBall> records, final IVector2 kickDir)
		{
			this.records = records;
			this.kickDir = kickDir;
		}


		@Override
		public double value(final double[] point)
		{
			final long tZero = records.get(0).getTimestamp();
			final IVector2 kickPos = Vector2.fromXY(point[0], point[1]);
			final IVector2 kickVel = kickDir.scaleToNew(point[2]);

			var traj = Geometry.getBallFactory()
					.createTrajectoryFromKickedBallWithoutSpin(kickPos, kickVel.getXYZVector());

			double error = 0;
			for (CamBall ball : records)
			{
				IVector2 modelPos = traj.getMilliStateAtTime((ball.getTimestamp() - tZero) * 1e-9).getPos()
						.getXYVector();

				error += modelPos.distanceTo(ball.getFlatPos());
			}

			error /= records.size();

			return error;
		}
	}
}
