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
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author AndreR <andre@ryll.cc>
 */
@Log4j2
public class StraightKickSolverNonLinIdentDirect
{
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public Optional<IBallModelIdentResult> identModel(final List<CamBall> records)
	{
		List<IVector2> groundPos = records.stream()
				.map(CamBall::getFlatPos)
				.toList();

		Optional<ILineSegment> kickLine = Lines.regressionLineFromPointsList(groundPos);
		if (kickLine.isEmpty())
		{
			return Optional.empty();
		}

		List<TimeVelocityPair> velocities = calculateVelocities(records);

		double accSlide = Geometry.getBallParameters().getAccSlide();
		double accRoll = Geometry.getBallParameters().getAccRoll();
		double kSwitch = Geometry.getBallParameters().getKSwitch();

		double[] initialGuess = new double[] { velocities.getFirst().getVelocity(), accSlide, accRoll, kSwitch };
		double[] lowerBounds = new double[] { 100, -8000, -2000, 0.5 };
		double[] upperBounds = new double[] { 10000, -100, -10, 0.85 };

		CMAESOptimizer optimizer = new CMAESOptimizer(10000, 0.01, true, 10, 0, new MersenneTwister(), false, null);

		try
		{
			var model = new StraightBallModel(velocities);
			final PointValuePair optimum = optimizer.optimize(
					new MaxEval(20000),
					new ObjectiveFunction(model),
					GoalType.MINIMIZE,
					new InitialGuess(initialGuess),
					new SimpleBounds(lowerBounds, upperBounds),
					new CMAESOptimizer.Sigma(new double[] { 10, 10, 1, 0.01 }),
					new CMAESOptimizer.PopulationSize(50));

			double error = model.value(optimum.getPointRef());
			log.debug(
					"Optimizer used {} evaluations, {} iterations, error: {}",
					optimizer.getEvaluations(), optimizer.getIterations(), error
			);

			// Parameters: kickVel (abs), accSlide, accRoll, kSwitch
			for (int i = 0; i < lowerBounds.length; i++)
			{
				double val = optimum.getPointRef()[i];
				if (val <= lowerBounds[i] || val >= upperBounds[i])
				{
					log.debug("Solution discarded, value {} with index {} is at boundary.", val, i);
					return Optional.empty();
				}
			}

			return Optional.of(new StraightModelIdentResult(
					kickLine.get().directionVector(),
					records.getFirst().getFlatPos(),
					records.getFirst().getTimestamp(),
					optimum.getPointRef(),
					records.size())
			);
		} catch (IllegalStateException | MathIllegalArgumentException e)
		{
			log.debug("No solution found", e);
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
				double deltaTime = (next.getTimestamp() - prev.getTimestamp()) * 1e-9;
				if (deltaTime < 1e-3)
				{
					continue;
				}

				long centralTime = (next.getTimestamp() + prev.getTimestamp()) / 2;

				timestampVelocityList.add(new TimeVelocityPair(centralTime, deltaPos / deltaTime));
			}
		}

		timestampVelocityList.sort(Comparator.comparingLong(TimeVelocityPair::getTimestamp));

		return timestampVelocityList;
	}


	public static class StraightModelIdentResult implements IBallModelIdentResult
	{
		private final IVector3 kickVel;
		private final IVector2 kickPos;
		private final long kickTimestamp;

		private final double accSlide;
		private final double accRoll;
		private final double kSwitch;

		@Getter
		private final int sampleAmount;


		/**
		 * @param kickDir
		 * @param kickPos
		 * @param kickTimestamp
		 * @param params
		 */
		public StraightModelIdentResult(final IVector2 kickDir, final IVector2 kickPos, final long kickTimestamp,
				final double[] params, final int sampleAmount)
		{
			kickVel = kickDir.scaleToNew(params[0]).getXYZVector();
			this.kickPos = kickPos;
			this.kickTimestamp = kickTimestamp;
			accSlide = params[1];
			accRoll = params[2];
			kSwitch = params[3];
			this.sampleAmount = sampleAmount;
		}


		public static String[] getParameterNames()
		{
			return new String[] { "accSlide", "accRoll", "kSwitch" };
		}


		@Override
		public EBallModelIdentType getType()
		{
			return EBallModelIdentType.STRAIGHT_TWO_PHASE;
		}


		@Override
		public Map<String, Double> getModelParameters()
		{
			Map<String, Double> params = new HashMap<>();
			params.put("accSlide", accSlide);
			params.put("accRoll", accRoll);
			params.put("kSwitch", kSwitch);

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


		public double getAccSlide()
		{
			return accSlide;
		}


		public double getAccRoll()
		{
			return accRoll;
		}


		@Override
		public String toString()
		{
			return "kickVel: " + kickVel + System.lineSeparator()
					+ "acc: " + accSlide + ", " + accRoll + System.lineSeparator()
					+ "kSwitch: " + kSwitch;
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
			final long tZero = velocities.getFirst().getTimestamp();

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

				error += Math.pow(entry.getVelocity() - modelVel, 2);
			}

			error = Math.sqrt(error);
			return error / velocities.size();
		}
	}
}
