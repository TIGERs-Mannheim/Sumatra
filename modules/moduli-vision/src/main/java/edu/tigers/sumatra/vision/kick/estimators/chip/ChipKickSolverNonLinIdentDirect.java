/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.chip;

import edu.tigers.sumatra.ball.trajectory.BallFactory;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.KickSolverResult;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Estimate kick velocity and chip parameters over complete chip ball trajectory
 * via a genetic optimization algorithm.
 */
public class ChipKickSolverNonLinIdentDirect extends AChipKickSolver
{
	private static final double[] LOWER_BOUNDS = new double[] { -6500, -6500, 100, 0.1, 0.1, 0.1 };
	private static final double[] UPPER_BOUNDS = new double[] { 6500, 6500, 6500, 0.95, 1.0, 0.9 };

	private double[] initialGuess = new double[6];


	/**
	 * @param kickPosition
	 * @param kickTimestamp
	 * @param camCalib
	 * @param initialEstimate
	 */
	public ChipKickSolverNonLinIdentDirect(final IVector2 kickPosition, final long kickTimestamp,
			final Map<Integer, CamCalibration> camCalib, final IVector3 initialEstimate)
	{
		super(kickPosition, kickTimestamp, camCalib);

		this.kickTimestamp = kickTimestamp;

		BallParameters ballParams = Geometry.getBallParameters();
		initialGuess[0] = initialEstimate.x();
		initialGuess[1] = initialEstimate.y();
		initialGuess[2] = initialEstimate.z();
		initialGuess[3] = ballParams.getChipDampingXYFirstHop();
		initialGuess[4] = ballParams.getChipDampingXYOtherHops();
		initialGuess[5] = ballParams.getChipDampingZ();
	}


	@Override
	public Optional<KickSolverResult> solve(final List<CamBall> records)
	{
		Optional<IBallModelIdentResult> result = identModel(records);
		return result.map(iBallModelIdentResult -> new KickSolverResult(kickPosition,
				iBallModelIdentResult.getKickVelocity(), kickTimestamp, getClass().getSimpleName()));
	}


	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<IBallModelIdentResult> identModel(final List<CamBall> records)
	{
		double[] result;

		// pull out wide-scatter shotgun
		CMAESOptimizer optimizer = new CMAESOptimizer(10000, 0.1, true, 10, 0, new MersenneTwister(), false, null);

		try
		{
			// and now fire it onto our problem
			final PointValuePair optimum = optimizer.optimize(
					new MaxEval(20000),
					new ObjectiveFunction(new ChipBallModel(records)),
					GoalType.MINIMIZE,
					new InitialGuess(initialGuess),
					new SimpleBounds(LOWER_BOUNDS, UPPER_BOUNDS),
					new CMAESOptimizer.Sigma(new double[] { 10, 10, 10, 0.01, 0.01, 0.01 }),
					new CMAESOptimizer.PopulationSize(50));

			result = optimum.getPoint();
		} catch (IllegalStateException | MathIllegalArgumentException e)
		{
			// victim did not survive
			return Optional.empty();
		}

		// we have a winner!
		IVector3 kickVelEst = Vector3.fromXYZ(result[0], result[1], result[2]);

		return Optional.of(new ChipModelIdentResult(kickVelEst, kickPosition, kickTimestamp,
				result[3], result[4], result[5]));
	}


	private class ChipBallModel implements MultivariateFunction
	{
		private final List<CamBall> records;
		private final edu.tigers.sumatra.ball.BallParameters ballParams;


		public ChipBallModel(final List<CamBall> records)
		{
			this.records = records;
			ballParams = Geometry.getBallFactory().getBallParams();
		}


		@Override
		public double value(final double[] point)
		{
			IVector3 kickVel = Vector3.fromXYZ(point[0], point[1], point[2]);
			double dampXYFirst = point[3];
			double dampXYOthers = point[4];
			double dampZ = point[5];

			var params = ballParams.toBuilder()
					.withChipDampingXYFirstHop(dampXYFirst)
					.withChipDampingXYOtherHops(dampXYOthers)
					.withChipDampingZ(dampZ)
					.build();

			var traj = new BallFactory(params)
					.createTrajectoryFromKickedBallWithoutSpin(kickPosition, kickVel);

			double error = 0;
			for (CamBall ball : records)
			{
				IVector3 trajPos = traj.getMilliStateAtTime((ball.gettCapture() - kickTimestamp) * 1e-9).getPos();
				IVector2 ground = trajPos.projectToGroundNew(getCameraPosition(ball.getCameraId()));

				error += ball.getFlatPos().distanceTo(ground);
			}

			error /= records.size();

			return error;
		}
	}

	public static class ChipModelIdentResult implements IBallModelIdentResult
	{
		private final IVector3 kickVel;
		private final IVector2 kickPos;
		private final long kickTimestamp;
		private final double dampingXYFirstHop;
		private final double dampingXYOtherHops;
		private final double dampingZ;


		/**
		 * @param kickVel
		 * @param kickPos
		 * @param kickTimestamp
		 * @param dampingXYFirstHop
		 * @param dampingXYOtherHops
		 * @param dampingZ
		 */
		public ChipModelIdentResult(final IVector3 kickVel, final IVector2 kickPos, final long kickTimestamp,
				final double dampingXYFirstHop,
				final double dampingXYOtherHops, final double dampingZ)
		{
			this.kickVel = kickVel;
			this.kickPos = kickPos;
			this.kickTimestamp = kickTimestamp;
			this.dampingXYFirstHop = dampingXYFirstHop;
			this.dampingXYOtherHops = dampingXYOtherHops;
			this.dampingZ = dampingZ;
		}


		public static String[] getParameterNames()
		{
			return new String[] { "dampXYFirst", "dampXYOther", "dampZ" };
		}


		@Override
		public EBallModelIdentType getType()
		{
			return EBallModelIdentType.CHIP_FIXED_LOSS_PLUS_ROLLING;
		}


		@Override
		public Map<String, Double> getModelParameters()
		{
			Map<String, Double> params = new HashMap<>();
			params.put("dampXYFirst", dampingXYFirstHop);
			params.put("dampXYOther", dampingXYOtherHops);
			params.put("dampZ", dampingZ);

			return params;
		}


		@Override
		public IVector3 getKickVelocity()
		{
			return kickVel;
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


		public double getDampingXYFirstHop()
		{
			return dampingXYFirstHop;
		}


		public double getDampingXYOtherHops()
		{
			return dampingXYOtherHops;
		}


		public double getDampingZ()
		{
			return dampingZ;
		}


		@Override
		public String toString()
		{
			return "kickVel: " + kickVel + System.lineSeparator()
					+ "dampXY: " + dampingXYFirstHop + ", " + dampingXYOtherHops + System.lineSeparator()
					+ "dampZ: " + dampingZ;
		}
	}
}
