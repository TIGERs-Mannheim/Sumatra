/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotVelDirectedToTargetChecker
{
	@Configurable(defValue = "0.2")
	private static double threshold = 0.2;
	
	static
	{
		ConfigRegistration.registerClass("skills", BotVelDirectedToTargetChecker.class);
	}
	
	
	public boolean check(final double targetAngle, final IVector2 botVel)
	{
		double diff = AngleMath.difference(botVel.getAngle(targetAngle), targetAngle);
		double velAbs = botVel.getLength2();
		return velAbs * diff < threshold;
	}
}
