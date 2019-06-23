/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import edu.tigers.sumatra.ai.math.kick.MovingRobotPassRater;
import edu.tigers.sumatra.ai.math.kick.StraightKickRater;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.WorldFrameFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShootScoreCalcPerfTest
{
	private final Random rng = new Random(0);
	
	
	@Test
	public void testRating()
	{
		WorldFrame wFrame = WorldFrameFactory.createWorldFrame(0, 0);
		List<ITrackedBot> bots = new ArrayList<>();
		for (int i = 0; i < 6; i++)
		{
			bots.add(WorldFrameFactory.createBot(0, BotID.createBotId(i, ETeamColor.BLUE)));
		}
		
		MovingRobotPassRater rater = new MovingRobotPassRater(bots);
		
		List<Long> durations1 = new ArrayList<>();
		List<Long> durations2 = new ArrayList<>();
		int n = 100000;
		double scoreSum1 = 0;
		double scoreSum2 = 0;
		for (int i = 0; i < n; i++)
		{
			IVector2 start = getRandomVector(3000);
			IVector2 target = getRandomVector(3000);
			long t0 = System.nanoTime();
			double score2 = StraightKickRater.rateStraightGoalKick(wFrame.getBots().values(), start, target);
			long t1 = System.nanoTime();
			double score1 = rater.rateLine(start, target, 3);
			long duration1 = t1 - t0;
			long duration2 = System.nanoTime() - t1;
			durations1.add(duration1);
			durations2.add(duration2);
			scoreSum1 += score1;
			scoreSum2 += score2;
		}
		System.out.println("Score Sum: " + scoreSum1 + ", mean: " + (scoreSum1 / n));
		System.out.println("Score Sum: " + scoreSum2 + ", mean: " + (scoreSum2 / n));
		System.out.println(durations1.stream().mapToDouble(e -> e).average().getAsDouble());
		System.out.println(durations2.stream().mapToDouble(e -> e).average().getAsDouble());
	}
	
	// private double rateLine(IVector2 start, IVector2 target)
	// {
	//
	// }
	
	
	private IVector2 getRandomVector(final double minmax)
	{
		return Vector2.fromXY(getRandomDouble(minmax), getRandomDouble(minmax));
	}
	
	
	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}
	
}