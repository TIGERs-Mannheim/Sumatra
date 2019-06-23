/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Abstract base class for all redirect consultants
 */
public abstract class ARedirectConsultant
{
	protected final IVector2 incomingBallVel;
	protected final IVector2 desiredOutgoingBallVel;
	
	
	protected ARedirectConsultant(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		this.incomingBallVel = incomingBallVel;
		this.desiredOutgoingBallVel = desiredOutgoingBallVel;
	}
	
	
	public abstract double getKickSpeed();
	
	
	public abstract double getTargetAngle();
	
	
	double getRedirectAngle()
	{
		IVector2 invIncomingBallVel = incomingBallVel.multiplyNew(-1);
		return invIncomingBallVel.angleTo(desiredOutgoingBallVel).orElse(0.0);
	}
}
