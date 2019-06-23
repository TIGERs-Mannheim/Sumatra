/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.chip;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.ChipBallTrajectory;
import edu.tigers.sumatra.vision.data.KickSolverResult;


/**
 * Estimate kick velocity and chip parameters over complete chip ball trajectory
 * via a genetic optimization algorithm.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ChipKickSolverNonLinIdentDirect extends AChipKickSolver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(ChipKickSolverNonLinIdentDirect.class.getName());
	
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
		Optional<ChipModelIdentResult> result = identModel(records);
		if (!result.isPresent())
		{
			return Optional.empty();
		}
		
		return Optional.of(new KickSolverResult(kickPosition, result.get().getKickVel(), kickTimestamp));
	}
	
	
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<ChipModelIdentResult> identModel(final List<CamBall> records)
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
		} catch (IllegalStateException e)
		{
			// victim did not survive
			return Optional.empty();
		}
		
		// we have a winner!
		IVector3 kickVelEst = Vector3.fromXYZ(result[0], result[1], result[2]);
		
		return Optional.of(new ChipModelIdentResult(kickVelEst, result[3], result[4], result[5]));
	}
	
	private class ChipBallModel implements MultivariateFunction
	{
		private final List<CamBall> records;
		
		
		public ChipBallModel(final List<CamBall> records)
		{
			this.records = records;
		}
		
		
		@Override
		public double value(final double[] point)
		{
			IVector3 kickVel = Vector3.fromXYZ(point[0], point[1], point[2]);
			double dampXYFirst = point[3];
			double dampXYOthers = point[4];
			double dampZ = point[5];
			double accRoll = Geometry.getBallParameters().getAccRoll();
			
			ChipBallTrajectory traj = new ChipBallTrajectory(kickPosition, kickVel, kickTimestamp,
					dampXYFirst, dampXYOthers, dampZ, accRoll);
			
			double error = 0;
			for (CamBall ball : records)
			{
				IVector3 trajPos = traj.getStateAtTimestamp(ball.gettCapture()).getPos();
				IVector2 ground = trajPos.projectToGroundNew(getCameraPosition(ball.getCameraId()));
				
				error += ball.getFlatPos().distanceTo(ground);
			}
			
			error /= records.size();
			
			return error;
		}
	}
	
	public static class ChipModelIdentResult
	{
		private final IVector3 kickVel;
		private final double dampingXYFirstHop;
		private final double dampingXYOtherHops;
		private final double dampingZ;
		
		
		/**
		 * @param kickVel
		 * @param dampingXYFirstHop
		 * @param dampingXYOtherHops
		 * @param dampingZ
		 */
		public ChipModelIdentResult(final IVector3 kickVel, final double dampingXYFirstHop,
				final double dampingXYOtherHops,
				final double dampingZ)
		{
			this.kickVel = kickVel;
			this.dampingXYFirstHop = dampingXYFirstHop;
			this.dampingXYOtherHops = dampingXYOtherHops;
			this.dampingZ = dampingZ;
		}
		
		
		public IVector3 getKickVel()
		{
			return kickVel;
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
			final StringBuilder builder = new StringBuilder();
			builder.append("kickVel: " + kickVel + System.lineSeparator());
			builder.append("dampXY: " + dampingXYFirstHop + ", " + dampingXYOtherHops + System.lineSeparator());
			builder.append("dampZ: " + dampingZ);
			return builder.toString();
		}
	}
}
