package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Builder(toBuilder = true)
public class PassStats
{
	/**
	 * First Map key is the originating zone.
	 * Second Map key is the target zone
	 */
	@Getter
	final Map<EOffensiveZone, Map<EOffensiveZone, ZonePassStats>> zonePassSuccessMap;

	@Getter
	final List<PassStatsKickReceiveVelocity> receiveVelocities;

	@Getter
	final int nPasses;

	@Getter
	final int successfulPasses;

	@Getter
	final int numPassLineReachedOnTime;


	public Map<Integer, Pair<Double, Integer>> getInterceptIntervalSuccessRatios(int groups)
	{
		var intervalData = new HashMap<Integer, List<Boolean>>();
		for (int i = 0; i < groups; i++)
		{
			intervalData.put(i, new ArrayList<>());
		}

		for (var passData : receiveVelocities)
		{
			var isSuccess = passData.success();
			var interceptScore = passData.scores().get(EPassRating.INTERCEPTION);
			if (interceptScore == null)
			{
				continue;
			}

			int getIdx = Math.clamp((int) Math.floor(interceptScore * groups), 0, groups - 1);
			intervalData.get(getIdx).add(isSuccess);
		}

		var returnData = new HashMap<Integer, Pair<Double, Integer>>();
		for (int i = 0; i < groups; i++)
		{
			returnData.put(
					i, new Pair<>(
							(double) intervalData.get(i).stream()
									.filter(e -> e)
									.count() / intervalData.get(i).size(), intervalData.get(i).size()
					)
			);
		}

		return returnData;
	}
}
