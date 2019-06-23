/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This redirect consultant adds a correction to the target angle based on a polynomial model
 */
class PolyCorrectionRedirectConsultant extends ARedirectConsultant
{
	@Configurable(comment = "f(angle,speed) = p00 + p10*angle + p01*speed", defValue = "0.08525;0.1801;-0.01201")
	private static Double[] parameters = new Double[] { 0.08525, 0.1801, -0.01201 };
	
	static
	{
		ConfigRegistration.registerClass("skills", PolyCorrectionRedirectConsultant.class);
	}
	
	
	PolyCorrectionRedirectConsultant(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		super(incomingBallVel, desiredOutgoingBallVel);
	}
	
	
	@Override
	public double getKickSpeed()
	{
		return desiredOutgoingBallVel.getLength2();
	}
	
	
	@Override
	public double getTargetAngle()
	{
		double redirectAngle = getRedirectAngle();
		double x = abs(redirectAngle);
		double y = desiredOutgoingBallVel.getLength2();
		double correction = parameters[0] + parameters[1] * x + parameters[2] * y;
		return desiredOutgoingBallVel.getAngle() - signum(redirectAngle) * correction;
	}
}
