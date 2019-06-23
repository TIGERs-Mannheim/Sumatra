/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 4, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import org.junit.Assert;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiMathTest
{
	static
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
	/**
	 */
	@Test
	public void testApproxOrientationBallDamp()
	{
		IVector2 shootVel = new Vector2(AngleMath.PI - 1).scaleTo(8);
		IVector2 incomingVec = new Vector2(AngleMath.PI_QUART).scaleTo(5);
		IVector2 targetVec = AiMath.ballDamp(shootVel, incomingVec, 0.004f);
		System.out.println("shootVel: " + shootVel.getAngle());
		System.out.println("incomingVec: " + incomingVec.getAngle());
		System.out.println("target angle: " + targetVec.getAngle());
		float targetOrientation = AiMath.approxOrientationBallDamp(8, incomingVec, AngleMath.PI_HALF + 0.1f,
				targetVec.getAngle(), 0.004f);
		Assert.assertEquals(AngleMath.PI - 1, targetOrientation, 0.1f);
	}
}
