/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.paramoptimizer;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.bot.DummyBot;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.paramoptimizer.redirect.RedirectParamCalc;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectParamCalcTest
{
	/**
	 */
	@Test
	public void testApproxOrientationBallDamp()
	{
		RedirectParamCalc calc = RedirectParamCalc.forBot(new DummyBot());
		IVector2 shootVel = new Vector2(AngleMath.PI - 1).scaleTo(8);
		IVector2 incomingVec = new Vector2(AngleMath.PI_QUART).scaleTo(5);
		IVector2 targetVec = calc.ballDamp(shootVel, incomingVec, 0.004, 0);
		System.out.println("shootVel: " + shootVel.getAngle());
		System.out.println("incomingVec: " + incomingVec.getAngle());
		System.out.println("target angle: " + targetVec.getAngle());
		double targetOrientation = calc.approxOrientation(8, incomingVec, AngleMath.PI_HALF + 0.1,
				targetVec.getAngle(), 0.004);
		Assert.assertEquals(AngleMath.PI - 1, targetOrientation, 0.1);
	}
}
