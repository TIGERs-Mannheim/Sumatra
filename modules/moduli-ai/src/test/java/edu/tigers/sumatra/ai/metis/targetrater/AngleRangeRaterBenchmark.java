/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.WorldFrameFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Benchmark for {@link AngleRangeRater}
 */
public class AngleRangeRaterBenchmark
{
	private final Random rng = new Random(0);


	public static void main(String[] args)
	{
		var b = new AngleRangeRaterBenchmark();
		for (int i = 0; i < 10; i++)
		{
			b.testRating();
		}
	}


	private void testRating()
	{
		List<ITrackedBot> bots = new ArrayList<>();
		for (int i = 0; i < 6; i++)
		{
			bots.add(WorldFrameFactory.createBot(0, BotID.createBotId(i, ETeamColor.BLUE)));
		}

		AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(bots);

		List<Long> durations1 = new ArrayList<>();
		List<Long> durations2 = new ArrayList<>();
		List<Long> durations3 = new ArrayList<>();
		int n = 100000;
		double scoreSum1 = 0;
		double scoreSum2 = 0;
		double scoreSum3 = 0;
		for (int i = 0; i < n; i++)
		{
			IVector2 start = getRandomVector(3000);
			long t0 = System.nanoTime();
			double score1 = BestDirectShotBallPossessingBot.getBestShot(Geometry.getGoalTheir(), start, bots)
					.map(ValuePoint::getValue).orElse(0.0);
			long t1 = System.nanoTime();
			double score2 = rater.rate(start).map(IRatedTarget::getScore).orElse(0.0);
			long t2 = System.nanoTime();
			double score3 = MaxAngleKickRater.getDirectShootScoreChance(bots, start);
			long t3 = System.nanoTime();
			long duration1 = t1 - t0;
			long duration2 = t2 - t1;
			long duration3 = t3 - t2;
			durations1.add(duration1);
			durations2.add(duration2);
			durations3.add(duration3);
			scoreSum1 += score1;
			scoreSum2 += score2;
			scoreSum3 += score3;
		}
		double avgDuration1 = durations1.stream().mapToDouble(e -> e).average().orElse(0.0);
		double avgDuration2 = durations2.stream().mapToDouble(e -> e).average().orElse(0.0);
		double avgDuration3 = durations3.stream().mapToDouble(e -> e).average().orElse(0.0);
		System.out.printf("1: Score Sum: %7.0f, Score Mean: %1.2f, Duration Mean: %9.2fns\n", scoreSum1, scoreSum1 / n,
				avgDuration1);
		System.out.printf("2: Score Sum: %7.0f, Score Mean: %1.2f, Duration Mean: %9.2fns\n", scoreSum2, scoreSum2 / n,
				avgDuration2);
		System.out.printf("3: Score Sum: %7.0f, Score Mean: %1.2f, Duration Mean: %9.2fns\n", scoreSum3, scoreSum3 / n,
				avgDuration3);
		System.out.println();
	}


	private IVector2 getRandomVector(final double minmax)
	{
		return Vector2.fromXY(getRandomDouble(minmax), getRandomDouble(minmax));
	}


	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}

}