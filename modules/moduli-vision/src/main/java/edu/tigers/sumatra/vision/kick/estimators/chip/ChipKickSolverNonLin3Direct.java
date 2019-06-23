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
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.data.ChipBallTrajectory;
import edu.tigers.sumatra.vision.data.KickSolverResult;


/**
 * Estimate kick velocity over complete chip ball trajectory via a simplex optimizer.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ChipKickSolverNonLin3Direct extends AChipKickSolver
{
	private double[] kickVelArray;
	private final SimplexOptimizer optimizer = new SimplexOptimizer(1e-3, 1e-3);
	private final NelderMeadSimplex simplex = new NelderMeadSimplex(3, 10.0);
	
	
	/**
	 * @param kickPosition
	 * @param kickTimestamp
	 * @param camCalib
	 * @param initialEstimate
	 */
	public ChipKickSolverNonLin3Direct(final IVector2 kickPosition, final long kickTimestamp,
			final Map<Integer, CamCalibration> camCalib, final IVector3 initialEstimate)
	{
		super(kickPosition, kickTimestamp, camCalib);
		
		this.kickTimestamp = kickTimestamp;
		kickVelArray = initialEstimate.toArray();
	}
	
	
	@Override
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<KickSolverResult> solve(final List<CamBall> records)
	{
		try
		{
			final PointValuePair optimum = optimizer.optimize(
					new MaxEval(20),
					new ObjectiveFunction(new ChipBallModel(records)),
					GoalType.MINIMIZE,
					new InitialGuess(kickVelArray),
					simplex);
			
			kickVelArray = optimum.getPoint();
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
			
			kickVelArray = sum;
		}
		
		// kick off speed, 3D!
		IVector3 kickVelEst = Vector3.fromArray(kickVelArray);
		
		return Optional.of(new KickSolverResult(kickPosition, kickVelEst, kickTimestamp));
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
			IVector3 kickVel = Vector3.fromArray(point);
			
			ChipBallTrajectory traj = new ChipBallTrajectory(kickPosition, kickVel, kickTimestamp);
			
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
}
