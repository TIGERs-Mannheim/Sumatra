/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickerModelTest
{
	/**
	 * 
	 */
	@Test
	@Ignore
	public void test1()
	{
		KickerModel model = KickerModel.forBot(EBotType.GRSIM);
		for (int i = 1000; i < 5000; i += 100)
		{
			double kickSpeed = model.getKickSpeed(i);
			int estDuration = (int) model.getDuration(kickSpeed);
			System.out.println(i + " " + kickSpeed + " " + estDuration);
			Assert.assertEquals(i, estDuration, 200);
		}
	}
	
	
	/**
	 * 
	 */
	@Test
	@Ignore
	public void test2()
	{
		KickerModel model = KickerModel.forBot(EBotType.GRSIM);
		double endVel = 1.5;
		for (double dist = 1000; dist < 7000; dist += 100)
		{
			double kickSpeed = Geometry.getBallModel().getVelForDist(dist, endVel);
			int dur = (int) model.getDuration(kickSpeed);
			System.out.println(dist + " " + endVel + " " + kickSpeed + " " + dur);
		}
	}
}
