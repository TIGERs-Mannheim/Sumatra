/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * The simple redirect consultant assumes that the bot can look to the target and can kick with desired kickSpeed
 */
class SimpleRedirectConsultant extends ARedirectConsultant
{
	SimpleRedirectConsultant(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
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
		return desiredOutgoingBallVel.getAngle();
	}
}
