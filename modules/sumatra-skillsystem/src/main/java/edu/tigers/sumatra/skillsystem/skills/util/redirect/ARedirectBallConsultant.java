/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.redirect;


import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Once this class is created, it trigger it's child to calculate angle and kick speed ONCE
 * It defines the interface for a redirect ball consultant.
 * Additionally it provides the information for the calculation of the redirect kick speed and redirect bot angle
 *
 * @author chris
 */
public abstract class ARedirectBallConsultant
{
	
	protected final double ballVel;
	protected final double ballRedirectAngle;
	protected final double desiredVelocity;
	private double kickSpeed;
	private double botRelativeTargetAngle;
	private double botAngleOffset;

	
	/**
	 * Creates an RedirectBallConsultant for this specific parameters
	 *
	 * @param ballVelocity at collision
	 * @param desiredBallVelocity after redirect, where the length represents the actual velocity
	 */
	ARedirectBallConsultant(IVector2 ballVelocity, IVector2 desiredBallVelocity)
	{
		if (!ballVelocity.isZeroVector() && !desiredBallVelocity.isZeroVector())
		{
			this.ballVel = ballVelocity.getLength2();
			this.ballRedirectAngle = desiredBallVelocity.angleTo(ballVelocity).orElse(0.);
			this.desiredVelocity = desiredBallVelocity.getLength2();
			this.botAngleOffset = ballVelocity.getAngle();
			update();
		} else
		{
			throw new IllegalArgumentException("Tried to calc redirect with a zero vectors");
		}
	}
	
	
	/**
	 * Creates an RedirectBallConsultant for this specific parameters
	 *
	 * @param ballVel ball velocity at collision
	 * @param desiredRedirectAngle angle between ball position, intersection and target position (-Pi|Pi)
	 * @param desiredVelocity desired ball velocity after redirect
	 */
	ARedirectBallConsultant(IVector2 ballVel, double desiredRedirectAngle, double desiredVelocity)
	{
		if (!ballVel.isZeroVector())
		{
			this.ballVel = ballVel.getLength2();
			this.botAngleOffset = ballVel.getAngle();
			this.ballRedirectAngle = desiredRedirectAngle;
			this.desiredVelocity = desiredVelocity;
			update();
		} else
		{
			throw new IllegalArgumentException("Tried to calc redirect with a zero ball velocity vector");
		}
	}
	
	
	/**
	 * This Method returns the kick speed necessary to get the desiredVelocity and desiredRedirectAngle.
	 *
	 * @return kickSpeed for redirect (m/s)
	 */
	public final double getRedirectKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * This Method returns the bot target angle necessary to get the desiredVelocity and desiredRedirectAngle.
	 *
	 * @return redirect angle
	 */
	public final double getBotTargetAngle()
	{
		return botAngleOffset - botRelativeTargetAngle;
	}
	
	
	private void update()
	{
		kickSpeed = calcRedirectKickSpeed();
		botRelativeTargetAngle = calcBotTargetAngle();
	}
	
	
	protected abstract double calcRedirectKickSpeed();
	
	
	protected abstract double calcBotTargetAngle();
	
	
}
