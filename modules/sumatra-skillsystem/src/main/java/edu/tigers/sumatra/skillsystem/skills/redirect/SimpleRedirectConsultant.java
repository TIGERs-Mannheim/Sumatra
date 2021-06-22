/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * The simple redirect consultant assumes that the bot can look to the target and can kick with desired kickSpeed
 */
class SimpleRedirectConsultant implements IRedirectConsultant
{
	@Override
	public double getKickSpeed(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		return desiredOutgoingBallVel.getLength2();
	}


	@Override
	public double getTargetAngle(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		return desiredOutgoingBallVel.getAngle();
	}
}
