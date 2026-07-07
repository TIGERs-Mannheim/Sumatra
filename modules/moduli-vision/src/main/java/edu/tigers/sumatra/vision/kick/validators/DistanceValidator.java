package edu.tigers.sumatra.vision.kick.validators;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Check if the ball was near the robot and is now away from it.
 *
 * @author AndreR
 */
public class DistanceValidator implements IKickValidator
{
	@Configurable(defValue = "160.0", comment = "At least one sample must be beyond this distance", unit = EConfigUnit.DISTANCE_MM)
	private static double	atLeastOneBeyondDist	= 160.0;

	@Configurable(defValue = "130.0", comment = "First sample must be closer, all others further away than this", unit = EConfigUnit.DISTANCE_MM)
	private static double	thresholdDist1			= 130.0;

	@Configurable(defValue = "170.0", comment = "First sample must be closer, all others further away than this (alternative)", unit = EConfigUnit.DISTANCE_MM)
	private static double	thresholdDist2			= 170.0;

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
					.toList();

			boolean distantBall = distances.stream().anyMatch(d -> d > atLeastOneBeyondDist);

			if ((distances.getFirst() < thresholdDist1)
					&& distances.subList(1, distances.size()).stream().allMatch(d -> d > thresholdDist1)
					&& distantBall)
			{
				return true;
			}

			if ((distances.getFirst() < thresholdDist2)
					&& distances.subList(1, distances.size()).stream().allMatch(d -> d > thresholdDist2)
					&& distantBall)
			{
				return true;
			}
		}

		return false;
	}
}
