/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickerModelTest
{
	static
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
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
			float kickSpeed = model.getKickSpeed(i);
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
		float endVel = 1.5f;
		for (float dist = 1000; dist < 7000; dist += 100)
		{
			float kickSpeed = AIConfig.getBallModel().getVelForDist(dist, endVel);
			int dur = (int) model.getDuration(kickSpeed);
			System.out.println(dist + " " + endVel + " " + kickSpeed + " " + dur);
		}
	}
}
