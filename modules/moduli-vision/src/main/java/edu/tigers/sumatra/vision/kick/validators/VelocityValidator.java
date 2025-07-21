/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.validators;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Check if raw camera balls all have a minimum distance.
 *
 * @author AndreR
 */
public class VelocityValidator implements IKickValidator
{
	@Configurable(defValue = "600.0", comment = "Minimum required ball velocity [mm/s]")
	private static double minVelocity = 600.0;

	static
	{
		ConfigRegistration.registerClass("vision", VelocityValidator.class);
	}


	@Override
	public String getName()
	{
		return "Vel";
	}


	@Override
	public boolean validateKick(final List<FilteredVisionBot> bots, final List<MergedBall> balls)
	{
		Map<Integer, List<MergedBall>> groupedBalls = balls.stream()
				.collect(Collectors.groupingBy((final MergedBall b) -> b.getLatestCamBall().get().getCameraId()));

		int validSamples = 0;

		for (List<MergedBall> group : groupedBalls.values())
		{
			for (int i = 1; i < group.size(); i++)
			{
				CamBall bPrev = group.get(i - 1).getLatestCamBall().orElseThrow(IllegalStateException::new);
				CamBall bNow = group.get(i).getLatestCamBall().orElseThrow(IllegalStateException::new);
				long tPrev = bPrev.getTimestamp();
				long tNow = bNow.getTimestamp();
				IVector2 prev = bPrev.getFlatPos();
				IVector2 now = bNow.getFlatPos();

				if (tPrev == tNow)
				{
					continue;
				}

				double vel = prev.distanceTo(now) / ((tNow - tPrev) * 1e-9);

				if (vel > minVelocity)
				{
					validSamples++;
				}
			}

			if (validSamples >= 2)
			{
				return true;
			}
		}

		return false;
	}
}
