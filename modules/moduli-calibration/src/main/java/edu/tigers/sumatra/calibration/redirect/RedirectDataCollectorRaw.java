/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.calibration.CalibrationDataSample;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Value;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class RedirectDataCollectorRaw implements IRedirectDataCollector
{
	private static final double MAX_REDIRECT_ANGLE = AngleMath.deg2rad(120);
	private static final double DT_BEFORE = 0.3;
	private static final double DT_AFTER = 0.3;

	private final List<CalibrationDataSample> buffer = new ArrayList<>();
	private long storeTimestamp;


	@Override
	public void stop()
	{
		buffer.clear();
	}


	@Override
	public Optional<RedirectSample> process(CalibrationDataSample sample)
	{
		buffer.removeIf(in -> (sample.getTimestamp() - in.getTimestamp()) / 1e9 > DT_BEFORE + DT_AFTER);
		buffer.add(sample);

		if (storeTimestamp == 0 && sample.isHasBallContact() && sample.getKickSpeed() > 0)
		{
			storeTimestamp = sample.getTimestamp();
		}
		double dt = (sample.getTimestamp() - storeTimestamp) / 1e9;
		if (storeTimestamp != 0 && dt > DT_BEFORE)
		{
			storeTimestamp = 0;
			return generate(new ArrayList<>(buffer));
		}
		return Optional.empty();
	}


	private Optional<RedirectSample> generate(List<CalibrationDataSample> samples)
	{
		if (samples.stream().anyMatch(s -> s.getRawBallPos() == null))
		{
			// raw ball pos is required
			return Optional.empty();
		}

		CalibrationDataSample prevSample = null;
		List<VelTimestamp> vels = new ArrayList<>();
		for (var sample : samples)
		{
			if (prevSample != null)
			{
				var dt = (sample.getTimestamp() - prevSample.getTimestamp()) / 1e9;
				var vel = sample.getRawBallPos().subtractNew(prevSample.getRawBallPos()).multiply(1e-3 / dt);
				if (dt > 0 && vel.getLength2() > 0.7)
				{
					long timestamp = (sample.getTimestamp() + prevSample.getTimestamp()) / 2;
					IVector2 meanPos = sample.getRawBallPos().addNew(prevSample.getRawBallPos()).multiply(0.5);
					vels.add(new VelTimestamp(timestamp, vel, meanPos));
					prevSample = sample;
				}
			} else
			{
				prevSample = sample;
			}
		}

		var result = findMaxDirInDirOut(vels);
		if (result == null)
		{
			return Optional.empty();
		}

		var iDivide = result.iDivide;
		var dirIn = result.dirIn;
		var dirOut = result.dirOut;
		var angle = AngleMath.diffAbs(dirIn + AngleMath.DEG_180_IN_RAD, dirOut);

		var orientation = samples.get(iDivide).getBotOrientation();
		var angleIn = AngleMath.diffAbs(dirIn + AngleMath.DEG_180_IN_RAD, orientation);
		var angleOut = AngleMath.diffAbs(dirOut, orientation);
		var angleDiff = AngleMath.difference(angleOut, angleIn);
		var angleDiffFactor = angleOut / angleIn;

		var kickSpeed = samples.get(iDivide).getKickSpeed();
		var speedIn = regressVel(vels.subList(0, iDivide - 1), iDivide - 2);
		var speedOut = regressVel(vels.subList(iDivide, vels.size()), 0);

		if (angle > MAX_REDIRECT_ANGLE || kickSpeed <= 0)
		{
			return Optional.empty();
		}

		return Optional.of(RedirectSample.builder()
				.samplesPre(samples.subList(0, iDivide - 1))
				.samplesPost(samples.subList(iDivide, samples.size()))
				.dirIn(dirIn)
				.dirOut(dirOut)
				.angle(angle)
				.angleIn(angleIn)
				.angleOut(angleOut)
				.angleDiff(angleDiff)
				.angleDiffFactor(angleDiffFactor)
				.kickSpeed(kickSpeed)
				.speedIn(speedIn)
				.speedOut(speedOut)
				.build());
	}


	private static VelDirResult findMaxDirInDirOut(List<VelTimestamp> vels)
	{
		VelDirResult bestResult = null;
		for (int i = 3; i < vels.size() - 3; i++)
		{
			var result = findDirInDirOut(vels, i);
			if (bestResult == null || result.dirVariance < bestResult.dirVariance)
			{
				bestResult = result;
			}
		}
		return bestResult;
	}


	private static VelDirResult findDirInDirOut(List<VelTimestamp> vels, int iDivide)
	{
		IVector2 dirInSum = Vector2.zero();
		IVector2 dirOutSum = Vector2.zero();
		for (int i = 0; i < vels.size(); i++)
		{
			if (i <= iDivide)
			{
				dirInSum = dirInSum.addNew(vels.get(i).vel.normalizeNew());
			} else
			{
				dirOutSum = dirOutSum.addNew(vels.get(i).vel.normalizeNew());
			}
		}
		VelDirResult result = new VelDirResult();
		result.iDivide = iDivide;
		result.dirIn = dirInSum.getAngle();
		result.dirOut = dirOutSum.getAngle();

		for (int i = 0; i < vels.size(); i++)
		{
			double diff;
			if (i <= iDivide)
			{
				diff = AngleMath.difference(vels.get(i).vel.getAngle(), result.dirIn);
			} else
			{
				diff = AngleMath.difference(vels.get(i).vel.getAngle(), result.dirOut);
			}
			result.dirVariance += diff * diff;
		}
		result.dirVariance /= vels.size();
		return result;
	}


	private static double regressVel(List<VelTimestamp> velocities, int iPredict)
	{
		var regression = new SimpleRegression();
		long tBase = velocities.get(0).timestamp;
		for (var vel : velocities)
		{
			double t = (vel.timestamp - tBase) / 1e9;
			double v = vel.vel.getLength2();
			regression.addData(t, v);
		}
		double tPredict = (velocities.get(iPredict).timestamp - tBase) / 1e9;
		return regression.predict(tPredict);
	}


	private static class VelDirResult
	{
		int iDivide;
		double dirIn;
		double dirOut;
		double dirVariance = 0;
	}

	@Value
	public static class VelTimestamp
	{
		long timestamp;
		IVector2 vel;
		IVector2 pos;
	}
}
