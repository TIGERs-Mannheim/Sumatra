/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.WorldFrameFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author sabolc
 */
public class MaxAngleKickRaterTest
{
	
	private WorldFrame wf = null;
	private static final long SEED = 689;
	
	
	/**
	 * This method sets up a WorldFrame with a constant SEED for the Random function
	 */
	@Before
	public void setup()
	{
		WorldFrameFactory.setRandomSeed(SEED);
		wf = WorldFrameFactory.createWorldFrame(0, 0);
	}
	
	
	/**
	 * Tests if the probability to score a shot from the opposite goal line is not too high
	 */
	@Test
	public void OurLongDistanceTest()
	{
		double prob = MaxAngleKickRater.getDirectShootScoreChance(wf.getFoeBots().values(),
				Geometry.getGoalOur().getCenter());
		Assert.assertTrue(prob < 0.40);
	}
	
	
	@Test
	public void FoeLongDistanceTest()
	{
		double prob = MaxAngleKickRater.getFoeScoreChanceWithDefender(wf.getTigerBotsVisible().values(),
				Geometry.getGoalTheir().getCenter());
		Assert.assertTrue(prob < 0.40);
	}
	
	
	/**
	 * Tests points in a half circle around the enemy goal to make sure the chance is not too low
	 */
	@Test
	public void OurShortDistanceTest()
	{
		double biggestProb = 0;
		int testpoints = 8; // actually tests n-2 points to avoid testing points on extended goal line
		IVector2 midpos = Geometry.getGoalTheir().getCenter();
		for (int i = 0; i < testpoints - 1; i++)
		{
			// test points in half circle around goal center
			double a = Math.PI / 2 + (i + 1) * (Math.PI / testpoints);
			
			IVector2 addvec = Vector2.fromAngle(a).scaleTo(Geometry.getPenaltyAreaDepth());
			IVector2 testpos = midpos.addNew(addvec);
			
			double prob = MaxAngleKickRater.getDirectShootScoreChance(wf.getFoeBots().values(), testpos);
			biggestProb = Math.max(biggestProb, prob);
		}
		Assert.assertTrue(biggestProb > 0.9);
	}
	
	
	@Test
	public void FoeShortDistanceTest()
	{
		double biggestProb = 0;
		int testpoints = 8; // actually tests n-2 points to avoid testing points on extended goal line
		IVector2 midpos = Geometry.getGoalOur().getCenter();
		for (int i = 0; i < testpoints - 1; i++)
		{
			// test points in half circle around goal center
			double a = Math.PI / 2 - (i + 1) * (Math.PI / testpoints);
			
			IVector2 addvec = Vector2.fromAngle(a).scaleTo(Geometry.getPenaltyAreaDepth());
			IVector2 testpos = midpos.addNew(addvec);
			
			double prob = MaxAngleKickRater.getFoeScoreChanceWithDefender(wf.getTigerBotsVisible().values(), testpos);
			biggestProb = Math.max(biggestProb, prob);
		}
		Assert.assertTrue(biggestProb > 0.9);
	}
	
	
	/**
	 * Tests if the probability to score a shot from behind a bot is near 0
	 */
	@Test
	public void OurBehindBotsTest()
	{
		for (ITrackedBot bot : wf.getFoeBots().values())
		{
			IVector2 dir = Vector2.fromPoints(Geometry.getGoalTheir().getCenter(), bot.getPos());
			dir = dir.scaleToNew(Geometry.getBotRadius() + Geometry.getBallRadius());
			IVector2 pos = bot.getPos().addNew(dir);
			
			double prob = MaxAngleKickRater.getDirectShootScoreChance(wf.getFoeBots().values(), pos);
			
			Assert.assertTrue(prob < 1e-3);
		}
	}
	
	
	@Test
	public void FoeBehindBotsTest()
	{
		for (ITrackedBot bot : wf.getTigerBotsVisible().values())
		{
			IVector2 dir = Vector2.fromPoints(Geometry.getGoalOur().getCenter(), bot.getPos());
			dir = dir.scaleToNew(Geometry.getBotRadius() + Geometry.getBallRadius());
			IVector2 pos = bot.getPos().addNew(dir);
			
			double prob = MaxAngleKickRater.getFoeScoreChanceWithDefender(wf.getTigerBotsVisible().values(), pos);
			
			Assert.assertTrue(prob < 1e-3);
		}
	}
}