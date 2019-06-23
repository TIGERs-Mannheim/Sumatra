/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.straight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.geometry.Geometry;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class StraightKickSolverNonLinIdentDirect
{
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<StraightModelIdentResult> identModel(final List<CamBall> records)
	{
		List<TimeVelocityPair> velocities = calculateVelocities(records);
		
		double accSlide = Geometry.getBallParameters().getAccSlide();
		double accRoll = Geometry.getBallParameters().getAccRoll();
		double kSwitch = Geometry.getBallParameters().getkSwitch();
		
		double[] initialGuess = new double[] { velocities.get(0).getVelocity(), accSlide, accRoll, kSwitch };
		double[] lowerBounds = new double[] { 100, -8000, -2000, 0.5 };
		double[] upperBounds = new double[] { 8000, -100, -10, 0.8 };
		
		CMAESOptimizer optimizer = new CMAESOptimizer(10000, 0.1, true, 10, 0, new MersenneTwister(), false, null);
		
		try
		{
			final PointValuePair optimum = optimizer.optimize(
					new MaxEval(20000),
					new ObjectiveFunction(new StraightBallModel(velocities)),
					GoalType.MINIMIZE,
					new InitialGuess(initialGuess),
					new SimpleBounds(lowerBounds, upperBounds),
					new CMAESOptimizer.Sigma(new double[] { 10, 10, 1, 0.01 }),
					new CMAESOptimizer.PopulationSize(50));
			
			return Optional.of(new StraightModelIdentResult(optimum.getPointRef()));
		} catch (IllegalStateException | MathIllegalArgumentException e)
		{
			return Optional.empty();
		}
	}
	
	
	private List<TimeVelocityPair> calculateVelocities(final List<CamBall> balls)
	{
		Map<Integer, List<CamBall>> groupedBalls = balls.stream()
				.collect(Collectors.groupingBy(CamBall::getCameraId));
		
		List<TimeVelocityPair> timestampVelocityList = new ArrayList<>();
		
		for (List<CamBall> ballList : groupedBalls.values())
		{
			for (int i = 1; i < ballList.size(); i++)
			{
				CamBall prev = ballList.get(i - 1);
				CamBall next = ballList.get(i);
				double deltaPos = prev.getFlatPos().distanceTo(next.getFlatPos());
				double deltaTime = (next.gettCapture() - prev.gettCapture()) * 1e-9;
				if (deltaTime < 1e-3)
				{
					continue;
				}
				
				long centralTime = (next.gettCapture() + prev.gettCapture()) / 2;
				
				timestampVelocityList.add(new TimeVelocityPair(centralTime, deltaPos / deltaTime));
			}
		}
		
		timestampVelocityList.sort((e1, e2) -> Long.compare(e1.getTimestamp(), e2.getTimestamp()));
		
		return timestampVelocityList;
	}
	
	public static class StraightModelIdentResult
	{
		private final double kickVel;
		private final double accSlide;
		private final double accRoll;
		private final double kSwitch;
		
		
		/**
		 * @param kickVel
		 * @param accSlide
		 * @param accRoll
		 * @param kSwitch
		 */
		public StraightModelIdentResult(final double kickVel, final double accSlide, final double accRoll,
				final double kSwitch)
		{
			this.kickVel = kickVel;
			this.accSlide = accSlide;
			this.accRoll = accRoll;
			this.kSwitch = kSwitch;
		}
		
		
		private StraightModelIdentResult(final double[] params)
		{
			kickVel = params[0];
			accSlide = params[1];
			accRoll = params[2];
			kSwitch = params[3];
		}
		
		
		public double getKickVel()
		{
			return kickVel;
		}
		
		
		public double getAccSlide()
		{
			return accSlide;
		}
		
		
		public double getAccRoll()
		{
			return accRoll;
		}
		
		
		public double getkSwitch()
		{
			return kSwitch;
		}
		
		
		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("kickVel: " + kickVel + System.lineSeparator());
			builder.append("acc: " + accSlide + ", " + accRoll + System.lineSeparator());
			builder.append("kSwitch: " + kSwitch);
			return builder.toString();
		}
	}
	
	private static class TimeVelocityPair
	{
		private final long timestamp;
		private final double velocity;
		
		
		private TimeVelocityPair(final long timestamp, final double velocity)
		{
			this.timestamp = timestamp;
			this.velocity = velocity;
		}
		
		
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
		public double getVelocity()
		{
			return velocity;
		}
	}
	
	private static class StraightBallModel implements MultivariateFunction
	{
		private final List<TimeVelocityPair> velocities;
		
		
		private StraightBallModel(final List<TimeVelocityPair> velocities)
		{
			this.velocities = velocities;
		}
		
		
		@Override
		public double value(final double[] point)
		{
			final double vKick = point[0];
			final double accSlide = point[1];
			final double accRoll = point[2];
			final double cSw = point[3];
			
			final double vSwitch = vKick * cSw;
			final double tSwitch = (vKick * (cSw - 1)) / accSlide;
			final long tZero = velocities.get(0).getTimestamp();
			
			double error = 0;
			
			for (TimeVelocityPair entry : velocities)
			{
				double t = (entry.getTimestamp() - tZero) * 1e-9;
				
				double modelVel;
				if (t < tSwitch)
				{
					modelVel = vKick + (t * accSlide);
				} else
				{
					modelVel = vSwitch + ((t - tSwitch) * accRoll);
				}
				
				error += Math.abs(entry.getVelocity() - modelVel);
			}
			
			return error;
		}
	}
}
