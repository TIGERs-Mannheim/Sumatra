/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.tracking;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Track the trajectory and the measured state and take measures from the data, like measuring the latency.
 */
public class RobotTrajectoryTracker
{
	private static final Logger log = LogManager.getLogger(RobotTrajectoryTracker.class.getName());

	private static final long STEP_SIZE = 1_000_000;

	private final DataSync<State> target;
	private final DataSync<State> measured;

	public RobotTrajectoryTracker(final double horizon)
	{
		target = new DataSync<>(horizon);
		measured = new DataSync<>(horizon);
	}


	public void addTarget(final long timestamp, final State state)
	{
		target.add(timestamp, state);
	}


	public void addMeasure(final long timestamp, final State state)
	{
		measured.add(timestamp, state);
	}


	public List<StateDifference> difference(final double latency)
	{
		long tEnd = getLastTimestamp();
		long tStart = getFirstTimestamp();

		List<StateDifference> stateDifferences = new ArrayList<>();
		for (long t = tEnd; t >= tStart; t -= STEP_SIZE)
		{
			getStateDifference(t, latency).ifPresent(stateDifferences::add);
		}
		return stateDifferences;
	}


	public void export(String fileName, double latency)
	{
		long tEnd = getLastTimestamp();
		long tStart = getFirstTimestamp();

		try (FileWriter fileWriter = new FileWriter(fileName, false))
		{
			for (long t = tStart; t <= tEnd; t += STEP_SIZE)
			{
				State measuredState = getMeasuredState(t, 0.0).orElse(State.zero());
				State targetState = getTargetState(t, 0.0).orElse(State.zero());
				State targetStateCorrected = getTargetState(t, latency).orElse(State.zero());
				List<Number> numbers = new ArrayList<>(18);
				numbers.add(t);
				numbers.addAll(measuredState.getNumberList());
				numbers.addAll(targetState.getNumberList());
				numbers.addAll(targetStateCorrected.getNumberList());
				fileWriter.write(StringUtils.join(numbers, ","));
				fileWriter.write("\n");
			}
			fileWriter.write("\n");
		} catch (IOException e)
		{
			log.warn("Could not export data", e);
		}
		log.info("Exported to {}", fileName);
	}


	private long getFirstTimestamp()
	{
		return target.getOldest().map(DataSync.DataStore::getTimestamp).orElse(0L);
	}


	private long getLastTimestamp()
	{
		return target.getLatest().map(DataSync.DataStore::getTimestamp).orElse(0L);
	}


	private Optional<StateDifference> getStateDifference(final long timestamp, final double latency)
	{
		final Optional<State> targetStateOpt = getTargetState(timestamp, latency);
		final Optional<State> measuredStateOpt = getMeasuredState(timestamp, 0.0);
		if (!targetStateOpt.isPresent() || !measuredStateOpt.isPresent())
		{
			return Optional.empty();
		}
		State targetState = targetStateOpt.get();
		State measuredState = measuredStateOpt.get();

		StateDifference stateDifference = new StateDifference();
		stateDifference.pos = targetState.getPos().subtractNew(measuredState.getPos());
		stateDifference.orientation = AngleMath.difference(targetState.getOrientation(),
				measuredState.getOrientation());
		stateDifference.vel = targetState.getVel2().subtractNew(measuredState.getVel2());
		stateDifference.aVel = AngleMath.difference(targetState.getAngularVel(), measuredState.getAngularVel());

		return Optional.of(stateDifference);
	}


	private Optional<State> getTargetState(final long timestamp, final double latency)
	{
		return getStateAtTime(timestamp, target, latency);
	}


	private Optional<State> getMeasuredState(final long timestamp, final double latency)
	{
		return getStateAtTime(timestamp, measured, latency);
	}


	private Optional<State> getStateAtTime(final long timestamp, final DataSync<State> dataSync, final double latency)
	{
		long latencyOffset = Math.round(latency * 1e9);
		return dataSync.get(timestamp - latencyOffset).map(p -> p.interpolate(timestamp - latencyOffset));
	}


	public Optional<StateDifference> differenceAverage(final double latency)
	{
		final List<StateDifference> differences = difference(latency);
		if (differences.isEmpty())
		{
			return Optional.empty();
		}
		StateDifference avgDifference = StateDifference.zero();
		for (StateDifference difference : differences)
		{
			avgDifference.pos = avgDifference.pos.addNew(difference.pos);
			avgDifference.orientation += difference.orientation;
			avgDifference.vel = avgDifference.vel.addNew(difference.vel);
			avgDifference.aVel += difference.aVel;
		}
		avgDifference.pos = avgDifference.pos.multiplyNew(1.0 / differences.size());
		avgDifference.orientation /= differences.size();
		avgDifference.vel = avgDifference.vel.multiplyNew(1.0 / differences.size());
		avgDifference.aVel /= differences.size();
		return Optional.of(avgDifference);
	}


	public Optional<StateDifference> differencePercentile(final double latency, final double percentile)
	{
		final List<StateDifference> differences = difference(latency);
		if (differences.isEmpty())
		{
			return Optional.empty();
		}
		int n2 = (int) Math.max(0, Math.min(differences.size() - 1L, Math.round(differences.size() * percentile)));
		StateDifference avgDifference = StateDifference.zero();
		differences.sort(Comparator.comparingDouble(d -> d.pos.x() + d.pos.y()));
		avgDifference.pos = differences.get(n2).pos;
		differences.sort(Comparator.comparingDouble(d -> d.orientation));
		avgDifference.orientation = differences.get(n2).orientation;

		differences.sort(Comparator.comparingDouble(d -> d.vel.x() + d.vel.y()));
		avgDifference.vel = differences.get(n2).vel;
		differences.sort(Comparator.comparingDouble(d -> d.aVel));
		avgDifference.aVel = differences.get(n2).aVel;

		return Optional.of(avgDifference);
	}


	public double estimateTimeDifferenceWithAvg()
	{
		return estimateTimeDifference(this::differenceAverage);
	}


	public double estimateTimeDifferenceWithMedian()
	{
		return estimateTimeDifference(t -> differencePercentile(t, 0.5));
	}


	public double estimateTimeDifference(Function<Double, Optional<StateDifference>> fn)
	{
		UnivariateOptimizer optimizer = new BrentOptimizer(0.0001, 0.001);
		UnivariateFunction func = t -> fn.apply(t).map(s -> s.pos.getLength2()).orElse(Double.MAX_VALUE);

		UnivariatePointValuePair result = optimizer.optimize(
				GoalType.MINIMIZE,
				new MaxEval(100),
				new MaxIter(100),
				new SearchInterval(-0.5, 0.5, 0.0),
				new UnivariateObjectiveFunction(func));

		return result.getPoint();
	}

	public static class StateDifference
	{
		IVector2 pos;
		double orientation;
		IVector2 vel;
		double aVel;

		static StateDifference zero()
		{
			StateDifference d = new StateDifference();
			d.pos = Vector2.zero();
			d.orientation = 0.0;
			d.vel = Vector2.zero();
			d.aVel = 0.0;
			return d;
		}


		@Override
		public String toString()
		{
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("pos", pos)
					.append("orientation", orientation)
					.append("vel", vel)
					.append("aVel", aVel)
					.toString();
		}
	}
}
