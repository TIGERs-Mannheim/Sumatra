package edu.tigers.sumatra.vision.kick.validators;

import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Check if all distances to the initial bot are increasing.
 *
 * @author AndreR
 */
public class IncreasingDistanceValidator implements IKickValidator
{

	@Override
	public String getName()
	{
		return "IncDist";
	}


	@Override
	public boolean validateKick(final List<FilteredVisionBot> bots, final List<MergedBall> balls)
	{
		FilteredVisionBot bot = bots.get(0);

		Map<Integer, List<MergedBall>> groupedBalls = balls.stream()
				.collect(Collectors.groupingBy((final MergedBall b) -> b.getLatestCamBall().get().getCameraId()));

		for (List<MergedBall> group : groupedBalls.values())
		{
			final List<Double> distances1 = group.stream()
					.map(ball -> ball.getLatestCamBall().get().getPos().getXYVector().distanceTo(bot.getPos()))
					.toList();

			if (distances1.size() < 2)
			{
				continue;
			}

			boolean valid = IntStream.range(1, distances1.size())
					.allMatch(i -> distances1.get(i) > distances1.get(i - 1));

			if (valid)
			{
				return true;
			}
		}

		return false;
	}
}
