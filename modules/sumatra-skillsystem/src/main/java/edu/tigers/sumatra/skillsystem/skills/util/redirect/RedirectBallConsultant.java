/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.redirect;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author chris
 */
class RedirectBallConsultant extends ARedirectBallConsultant
{
	
	/**
	 * Creates an RedirectBallConsultant for this specific parameters
	 *
	 * @param ballVelocity at collision
	 * @param desiredBallVelocity after redirect
	 */
	RedirectBallConsultant(final IVector2 ballVelocity, final IVector2 desiredBallVelocity)
	{
		super(ballVelocity, desiredBallVelocity);
	}
	
	
	/**
	 * Creates an RedirectBallConsultant for this specific parameters
	 *
	 * @param ballVel ball velocity at collision
	 * @param desiredRedirectAngle angle between ball position, intersection and target position (-Pi|Pi)
	 * @param desiredVelocity desired ball velocity after redirect
	 */
	RedirectBallConsultant(final IVector2 ballVel, final double desiredRedirectAngle,
			final double desiredVelocity)
	{
		super(ballVel, desiredRedirectAngle, desiredVelocity);
	}
	
	
	@Override
	protected double calcRedirectKickSpeed()
	{
		return RedirectBallModel.getDefaultInstance().calcKickSpeed(Vector2.fromX(ballVel),
				Vector2.fromAngle(ballRedirectAngle), desiredVelocity);
	}
	
	
	@Override
	protected double calcBotTargetAngle()
	{
		IVector2 ballVelocity = Vector2.fromX(ballVel);
		double kickSpeed = RedirectBallModel.getDefaultInstance().calcKickSpeed(ballVelocity,
				Vector2.fromAngle(ballRedirectAngle), desiredVelocity);
		return RedirectBallModel.getDefaultInstance().calcTargetOrientation(ballVelocity, kickSpeed, ballRedirectAngle,
				ballRedirectAngle);
	}
	
}
