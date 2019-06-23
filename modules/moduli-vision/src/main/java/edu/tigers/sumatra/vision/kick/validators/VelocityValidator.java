/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.validators;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.CamBallInternal;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;


/**
 * Check if raw camera balls all have a minimum distance.
 * 
 * @author AndreR
 */
public class VelocityValidator implements IKickValidator
{
	@Configurable(comment = "Minimum required ball velocity [mm/s]")
	private static double minVelocity = 800;
	
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
		
		for (List<MergedBall> group : groupedBalls.values())
		{
			boolean valid = true;
			for (int i = 1; i < group.size(); i++)
			{
				CamBallInternal bPrev = group.get(i - 1).getLatestCamBall().orElseThrow(IllegalStateException::new);
				CamBallInternal bNow = group.get(i).getLatestCamBall().orElseThrow(IllegalStateException::new);
				long tPrev = bPrev.gettCapture() + (long) (bPrev.getDtDeviation() * 1e9);
				long tNow = bNow.gettCapture() + (long) (bNow.getDtDeviation() * 1e9);
				IVector2 prev = bPrev.getFlatPos();
				IVector2 now = bNow.getFlatPos();
				
				if (tPrev == tNow)
				{
					continue;
				}
				
				double vel = prev.distanceTo(now) / ((tNow - tPrev) * 1e-9);
				
				if (vel < minVelocity)
				{
					valid = false;
				}
			}
			
			if (valid)
			{
				return true;
			}
		}
		
		return false;
	}
}
