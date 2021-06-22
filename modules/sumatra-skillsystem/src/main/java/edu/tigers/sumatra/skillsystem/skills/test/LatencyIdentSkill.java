/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.filter.IInterpolatable;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import lombok.Setter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import java.io.FileWriter;
import java.io.IOException;


/**
 * Identify the delay from giving a command to receiving the response from vision
 * by rotating with a Sinus speed.
 */
@Log4j2
public class LatencyIdentSkill extends AMoveSkill
{
	@Setter
	private double amplitude;
	@Setter
	private double frequency;
	@Setter
	private double duration;

	private int iteration;
	private long tStart;
	private final DataSync<DataSet> setSpeedSync = new DataSync<>(60);
	private final DataSync<DataSet> measuredSpeedSync = new DataSync<>(60);


	@Override
	public void doUpdate()
	{
		if (tStart == 0)
		{
			tStart = getWorldFrame().getTimestamp();
		}
		double t = (getWorldFrame().getTimestamp() - tStart) / 1e9;
		double speed = AngleMath.PI_TWO * amplitude * frequency * SumatraMath.sin(AngleMath.PI_TWO * frequency * t);
		setLocalVelocity(Vector2f.ZERO_VECTOR, speed, getTBot().getMoveConstraints());
		double measuredSpeed = getTBot().getFilteredState().orElseThrow().getAngularVel();
		setSpeedSync.add(getWorldFrame().getTimestamp(), new DataSet(speed));
		measuredSpeedSync.add(getWorldFrame().getTimestamp(), new DataSet(measuredSpeed));

		if (t >= duration)
		{
			evaluate(String.valueOf(iteration++));
			setSpeedSync.reset();
			measuredSpeedSync.reset();
			tStart = 0;
		}
	}


	@Override
	protected void doExitActions()
	{
		evaluate("n");
	}


	private void evaluate(String iterationId)
	{
		double latency = estimateTimeDifference();
		double error = shiftByTime(latency);
		log.info("{} - latency: {}, error: {}", iterationId, latency, error);

		long start = tStart + (long) 2e8;
		long end = getWorldFrame().getTimestamp() - (long) 2e8;
		try (FileWriter fw = new FileWriter("/tmp/data_" + iterationId + ".csv"))
		{
			for (long t = start; t < end; t += 1_000_000)
			{
				double measured = measuredSpeedSync.get(t).orElseThrow().interpolate(t).speed;
				long tShifted = t - (long) (latency * 1e9);
				double set = setSpeedSync.get(t).orElseThrow().interpolate(t).speed;
				double setShifted = setSpeedSync.get(tShifted).orElseThrow().interpolate(tShifted).speed;
				fw.write(t + " " + measured + " " + set + " " + setShifted + "\n");
			}
		} catch (IOException e)
		{
			log.warn("Could not write", e);
		}
	}


	private double estimateTimeDifference()
	{
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-7, 1e-10);

		UnivariatePointValuePair result = optimizer.optimize(
				GoalType.MINIMIZE,
				new MaxEval(100),
				new MaxIter(100),
				new SearchInterval(0.0, 0.1, 0.0),
				new UnivariateObjectiveFunction(this::shiftByTime));

		return result.getPoint();
	}


	private double shiftByTime(double timeShift)
	{
		long start = tStart + (long) 2e8;
		long end = getWorldFrame().getTimestamp() - (long) 2e8;
		double diffSum = 0;
		int n = 0;
		for (long t = start; t < end; t += 1_000_000)
		{
			double measured = measuredSpeedSync.get(t).orElseThrow().interpolate(t).speed;
			long tShifted = t - (long) (timeShift * 1e9);
			double set = setSpeedSync.get(tShifted).orElseThrow().interpolate(tShifted).speed;
			double diff = set - measured;
			diffSum = diff * diff;
			n++;
		}
		return diffSum / n;
	}


	@Value
	private static class DataSet implements IInterpolatable<DataSet>
	{
		double speed;


		@Override
		public DataSet interpolate(DataSet other, double percentage)
		{
			return new DataSet(speed * (1 - percentage) + other.speed * percentage);
		}
	}
}
