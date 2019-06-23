/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;


/**
 * Check if the ball was near the robot and is now away from it.
 * 
 * @author AndreR
 */
public class DistanceValidator implements IKickValidator
{
	@Configurable(comment = "At least one sample must be beyond this distance")
	private static double	atLeastOneBeyondDist	= 140;
	
	@Configurable(comment = "First sample must be closer, all others further away than this")
	private static double	thresholdDist1			= 110;
	
	@Configurable(comment = "First sample must be closer, all others further away than this (alternative)")
	private static double	thresholdDist2			= 130;
	
	static
	{
		ConfigRegistration.registerClass("vision", DistanceValidator.class);
	}
	
	
	@Override
	public String getName()
	{
		return "Dist";
	}
	
	
	@Override
	public boolean validateKick(final List<FilteredVisionBot> bots, final List<MergedBall> balls)
	{
		Map<Integer, List<Pair<MergedBall, FilteredVisionBot>>> dataById = new HashMap<>();
		
		for (int i = 0; i < bots.size(); i++)
		{
			dataById.putIfAbsent(balls.get(i).getLatestCamBall().get().getCameraId(), new ArrayList<>());
			dataById.get(balls.get(i).getLatestCamBall().get().getCameraId()).add(new Pair<>(balls.get(i), bots.get(i)));
		}
		
		for (List<Pair<MergedBall, FilteredVisionBot>> data : dataById.values())
		{
			List<Double> distances = data.stream()
					.map(d -> d.getFirst().getLatestCamBall().get().getPos().getXYVector()
							.distanceTo(d.getSecond().getPos()))
					.collect(Collectors.toList());
			
			boolean distantBall = false;
			if (distances.stream().anyMatch(d -> d > atLeastOneBeyondDist))
			{
				distantBall = true;
			}
			
			if ((distances.get(0) < thresholdDist1)
					&& distances.subList(1, distances.size()).stream().allMatch(d -> d > thresholdDist1)
					&& distantBall)
			{
				return true;
			}
			
			if ((distances.get(0) < thresholdDist2)
					&& distances.subList(1, distances.size()).stream().allMatch(d -> d > thresholdDist2)
					&& distantBall)
			{
				return true;
			}
		}
		
		return false;
	}
}
