/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.OffensiveZones;
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 * A Factory to create a {@link RatedPass} from a {@link Pass} with some {@link IPassRater}s
 */
public class RatedPassFactory
{

	private final Map<EPassRating, IPassRater> passRaters = new EnumMap<>(EPassRating.class);

	public RatedPassFactory()
	{
		passRaters.put(EPassRating.PRESSURE, new PassPressureRater());
		passRaters.put(EPassRating.PASSABILITY, new PassabilityRater());
	}


	public void update(Collection<ITrackedBot> consideredBots)
	{
		passRaters.put(EPassRating.INTERCEPTION, new PassInterceptionMovingRobotRater(consideredBots));
		passRaters.put(EPassRating.REFLECT_GOAL_KICK, new ReflectorRater(consideredBots));
		passRaters.put(EPassRating.GOAL_KICK, new GoalRater(consideredBots));
	}


	public void updateDynamic(Collection<ITrackedBot> consideredBots,
			PassStats passStats, OffensiveZones offensiveZones)
	{

		passRaters.put(EPassRating.INTERCEPTION, new DynamicPassInterceptionMovingRobotRater(consideredBots, passStats, offensiveZones));
		passRaters.put(EPassRating.REFLECT_GOAL_KICK, new ReflectorRater(consideredBots));
		passRaters.put(EPassRating.GOAL_KICK, new GoalRater(consideredBots));
	}


	public RatedPass rate(Pass pass)
	{
		var ratedPass = RatedPass.builder().pass(pass);
		passRaters.forEach((t, r) -> ratedPass.score(t, r.rate(pass)));
		return ratedPass.build();
	}


	public double rateMaxCombined(Pass pass, EPassRating... passRating)
	{
		return Arrays.stream(passRating)
				.map(passRaters::get)
				.mapToDouble(r -> r.rate(pass))
				.reduce(1, (a, b) -> a * b);
	}


	public void setShapes(List<IDrawableShape> shapes)
	{
		passRaters.values().forEach(r -> r.setShapes(shapes));
	}
}
