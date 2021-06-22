/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.kick.estimators.straight;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import edu.tigers.sumatra.vision.kick.estimators.IKickSolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class FlatKickSolverNonLin3Factor implements IKickSolver
{
	private static final int KICK_DIR_MAX_RECORDS_PER_CAM = 5;

	private final IVector2 initialSpin;
	private final double kickBotOrient;

	private double[] initialGuess = new double[3];
	private final SimplexOptimizer optimizer = new SimplexOptimizer(1e-3, 1e-3);
	private final NelderMeadSimplex simplex = new NelderMeadSimplex(3, 100.0);

	private Optional<IVector2> fixedKickDir = Optional.empty();


	public FlatKickSolverNonLin3Factor(final Pose kickingBotPose, final Optional<FilteredVisionBall> ballStateAtKick)
	{
		initialSpin = ballStateAtKick.map(FilteredVisionBall::getSpin).orElse(Vector2f.ZERO_VECTOR);
		kickBotOrient = kickingBotPose.getOrientation();
	}


	public Optional<IBallModelIdentResult> identModel(final List<CamBall> records)
	{
		if (!isRedirect())
		{
			return Optional.empty();
		}

		var kickDir = fixedKickDir.or(() -> getKickDir(records));
		if (kickDir.isEmpty())
		{
			return Optional.empty();
		}

		computeFixedKickDir(records);

		long tZero = records.get(0).gettCapture();

		double minError = Double.MAX_VALUE;
		double minFactor = 0.0;
		for (double f = 0.0; f <= 1.0; f += 0.01)
		{
			double error = nonLinSolve3Factor(records, kickDir.get(), initialSpin.multiplyNew(f)).getError();

			if (error < minError)
			{
				minError = error;
				minFactor = f;
			}
		}

		NonLinSolve3FactorResult result = nonLinSolve3Factor(records, kickDir.get(), initialSpin.multiplyNew(minFactor));

		double inboundAngle = AngleMath.difference(initialSpin.multiplyNew(-1).getAngle(), kickBotOrient);
		double inboundVelocity = initialSpin.multiplyNew(Geometry.getBallRadius()).getLength2();
		double outboundAngle = AngleMath.difference(result.getKickVel().getAngle(), kickBotOrient);
		double outboundVelocity = result.getKickVel().getLength2();

		return Optional.of(new RedirectModelIdentResult(result.getKickVel().getXYZVector(), result.kickPos, tZero,
				minFactor, inboundAngle, inboundVelocity, outboundAngle, outboundVelocity));
	}


	@Override
	public Optional<KickSolverResult> solve(final List<CamBall> records)
	{
		if (!isRedirect())
		{
			return Optional.empty();
		}

		var kickDir = fixedKickDir.or(() -> getKickDir(records));
		if (kickDir.isEmpty())
		{
			return Optional.empty();
		}

		computeFixedKickDir(records);

		long tZero = records.get(0).gettCapture();

		double redirectSpinFactor = Geometry.getBallParameters().getRedirectSpinFactor();
		NonLinSolve3FactorResult result = nonLinSolve3Factor(records, kickDir.get(),
				initialSpin.multiplyNew(redirectSpinFactor));

		return Optional.of(new KickSolverResult(result.getKickPos(), result.getKickVel().getXYZVector(), tZero,
				initialSpin.multiplyNew(redirectSpinFactor), getClass().getSimpleName()));
	}


	private boolean isRedirect()
	{
		// if initial spin not greater than an equal velocity of 1m/s => not a redirect
		return initialSpin.multiplyNew(Geometry.getBallRadius()).getLength2() > 1000.0;
	}


	private Optional<IVector2> getKickDir(final List<CamBall> records)
	{
		Map<Integer, List<CamBall>> groupedBalls = records.stream()
				.collect(Collectors.groupingBy(CamBall::getCameraId));

		List<IVector2> groundPos = groupedBalls.values().stream()
				.flatMap(List::stream)
				.map(CamBall::getFlatPos)
				.collect(Collectors.toList());

		Optional<Line> kickLine = Line.fromPointsList(groundPos);
		if (kickLine.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(kickLine.get().directionVector().normalizeNew());
	}


	private void computeFixedKickDir(final List<CamBall> records)
	{
		if (fixedKickDir.isPresent())
		{
			return;
		}

		Map<Integer, List<CamBall>> groupedBalls = records.stream()
				.collect(Collectors.groupingBy(CamBall::getCameraId));

		boolean atLeastOneGroupAtMaxRecords = groupedBalls.values().stream()
				.anyMatch(l -> l.size() >= KICK_DIR_MAX_RECORDS_PER_CAM);

		if (atLeastOneGroupAtMaxRecords)
		{
			// We need to store the kick dir at some point because records will get pruned and
			// this would screw up our kick dir calculation.
			fixedKickDir = getKickDir(records);
		}
	}


	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	private NonLinSolve3FactorResult nonLinSolve3Factor(final List<CamBall> records, final IVector2 kickDir,
			final IVector2 kickSpin)
	{
		double[] result;
		var model = new FlatKickSolverNonLin3Factor.StraightBallModel(records, kickSpin, kickDir);

		try
		{
			final PointValuePair optimum = optimizer.optimize(
					new MaxEval(50),
					new ObjectiveFunction(model),
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

		double error = model.value(result);

		IVector2 kickPos = Vector2.fromXY(result[0], result[1]);
		IVector2 kickVel = kickDir.scaleToNew(result[2]);

		return new NonLinSolve3FactorResult(kickPos, kickVel, error);
	}


	@Value
	private static class NonLinSolve3FactorResult
	{
		IVector2 kickPos;
		IVector2 kickVel;
		double error;
	}

	@RequiredArgsConstructor
	private static class StraightBallModel implements MultivariateFunction
	{
		private final List<CamBall> records;
		private final IVector2 kickSpin;
		private final IVector2 kickDir;


		@Override
		public double value(final double[] point)
		{
			final long tZero = records.get(0).gettCapture();
			final IVector2 kickPos = Vector2.fromXY(point[0], point[1]);
			final IVector2 kickVel = kickDir.scaleToNew(point[2]);

			var traj = Geometry.getBallFactory()
					.createTrajectoryFromKickedBall(kickPos, kickVel.getXYZVector(), kickSpin);

			double error = 0;
			for (CamBall ball : records)
			{
				IVector2 modelPos = traj.getMilliStateAtTime((ball.gettCapture() - tZero) * 1e-9).getPos().getXYVector();

				error += modelPos.distanceToSqr(ball.getFlatPos());
			}

			error /= records.size();

			return error;
		}
	}

	@RequiredArgsConstructor
	@Getter
	public static class RedirectModelIdentResult implements IBallModelIdentResult
	{
		private final IVector3 kickVel;
		private final IVector2 kickPos;
		private final long kickTimestamp;

		private final double spinFactor;
		private final double inAngle;
		private final double inVel;
		private final double outAngle;
		private final double outVel;


		public static String[] getParameterNames()
		{
			return new String[] { "spinFactor", "inAngle", "inVel", "outAngle", "outVel" };
		}


		@Override
		public EBallModelIdentType getType()
		{
			return EBallModelIdentType.REDIRECT;
		}


		@Override
		public IVector2 getKickPosition()
		{
			return kickPos;
		}


		@Override
		public long getKickTimestamp()
		{
			return kickTimestamp;
		}


		@Override
		public IVector3 getKickVelocity()
		{
			return kickVel;
		}


		@Override
		public Map<String, Double> getModelParameters()
		{
			Map<String, Double> params = new HashMap<>();
			params.put("spinFactor", spinFactor);
			params.put("inAngle", inAngle);
			params.put("inVel", inVel);
			params.put("outAngle", outAngle);
			params.put("outVel", outVel);

			return params;
		}


		@Override
		public String toString()
		{
			return "kickVel: " + kickVel + System.lineSeparator()
					+ "spinFactor: " + spinFactor + System.lineSeparator()
					+ "in: " + Vector2.fromAngleLength(inAngle, inVel) + System.lineSeparator()
					+ "out: " + Vector2.fromAngleLength(outAngle, outVel) + System.lineSeparator();
		}


		public IVector2 getInVelocity()
		{
			return Vector2.fromAngleLength(inAngle, inVel);
		}


		public IVector2 getOutVelocity()
		{
			return Vector2.fromAngleLength(outAngle, outVel);
		}
	}
}