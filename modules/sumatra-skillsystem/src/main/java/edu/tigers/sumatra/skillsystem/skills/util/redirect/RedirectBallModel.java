/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.redirect;

import static edu.tigers.sumatra.math.AngleMath.PI;
import static edu.tigers.sumatra.math.AngleMath.PI_HALF;
import static edu.tigers.sumatra.math.AngleMath.difference;
import static edu.tigers.sumatra.math.AngleMath.normalizeAngle;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectBallModel
{
	private static final int ORIENT_BALL_DAMP_MAX_ITER = 10;
	private static final double ORIENT_BALL_DAMP_ACCURACY = 1e-3;
	private static final int KICKSPEED_MAX_ITER = 10;
	private static final double KICKSPEED_ACCURACY = 0.05;
	private static final RedirectBallModel DEFAULT_INSTANCE = new RedirectBallModel();
	private static final double KICK_DAMP_FACTOR = 1;
	
	@Configurable(defValue = "0.0")
	private static double frontLossFactor = 0.0;
	
	static
	{
		ConfigRegistration.registerClass("skills", RedirectBallModel.class);
	}
	
	
	/**
	 * @return the default model
	 */
	public static RedirectBallModel getDefaultInstance()
	{
		return DEFAULT_INSTANCE;
	}
	
	
	static double calcKickSpeed(IVector2 ballVel, final IVector2 collisionNormal,
			final double targetBallVel, final double dampFactor)
	{
		double kickSpeed = targetBallVel;
		for (int i = 0; i < KICKSPEED_MAX_ITER; i++)
		{
			IVector2 outVec = kickerRedirect(ballVel, collisionNormal, dampFactor, kickSpeed);
			double diff = targetBallVel - outVec.getLength2();
			if (Math.abs(diff) < KICKSPEED_ACCURACY)
			{
				break;
			}
			kickSpeed += diff / 2;
		}
		return Math.max(0, kickSpeed);
	}
	
	
	static double ballDamp(IVector2 ballVel, final IVector2 collisionNormal, final double frontLoss)
	{
		double normalToCollAngle = ballVel.angleToAbs(collisionNormal.multiplyNew(-1)).orElse(0.0);
		double relDamp = Math.cos(normalToCollAngle);
		return relDamp * frontLoss;
	}
	
	
	static IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal, final double dampFactor)
	{
		double normalToCollAngle = ballVel.angleToAbs(collisionNormal).orElse(0.0);
		if (normalToCollAngle < PI_HALF)
		{
			// ball vel zero or wrong direction
			return ballVel;
		}
		
		double velInfAngle = ballVel.getAngle();
		if (normalToCollAngle > PI_HALF)
		{
			velInfAngle = ballVel.getAngle() + PI;
		}
		double velAngleDiff = difference(collisionNormal.getAngle(), velInfAngle);
		double damp = ballDamp(ballVel, collisionNormal, dampFactor);
		return collisionNormal.turnNew(velAngleDiff).scaleTo(
				ballVel.getLength2() * (1 - damp));
	}
	
	
	static IVector2 kickerRedirect(final IVector2 ballVel, final IVector2 collisionNormal,
			final double dampFactor,
			final double kickSpeed)
	{
		IVector2 dampedBallVel = ballCollision(ballVel, collisionNormal, dampFactor);
		return dampedBallVel.addNew(collisionNormal.scaleToNew(kickSpeed));
	}
	
	
	static double calcTargetOrientation(final IVector2 ballVel, final double shootSpeed,
			final double ballTargetDir, final double initialOrientation,
			final double ballDampFactor)
	{
		double destAngle = initialOrientation;
		for (int i = 0; i < ORIENT_BALL_DAMP_MAX_ITER; i++)
		{
			IVector2 destDir = Vector2.fromAngle(destAngle);
			IVector2 outVec = kickerRedirect(ballVel, destDir, ballDampFactor, shootSpeed);
			double diff = difference(ballTargetDir, outVec.getAngle());
			if (Math.abs(diff) < ORIENT_BALL_DAMP_ACCURACY)
			{
				break;
			}
			destAngle = destAngle + diff / 2;
		}
		return normalizeAngle(destAngle);
	}
	
	
	/**
	 * @param ballVel velocity of the ball when it hits the obstacle (direction + length)
	 * @param collisionNormal normal of the obstacle
	 * @return the resulting ball vel after hit
	 */
	IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal)
	{
		return ballCollision(ballVel, collisionNormal, frontLossFactor);
	}
	
	
	/**
	 * @param ballVel velocity of the ball when it hits the kicker (direction + length)
	 * @param collisionNormal normal of the kicker (from kick center towards outside)
	 * @param kickSpeed velocity of ball after kick (desired kick speed)
	 * @return resulting ball vel after kicker was hit
	 */
	public IVector2 kickerRedirect(final IVector2 ballVel, final IVector2 collisionNormal,
			final double kickSpeed)
	{
		return kickerRedirect(ballVel, collisionNormal, frontLossFactor, kickSpeed);
	}
	
	
	/**
	 * Calculate the required orientation of the bot in order to redirect the ball
	 * to the desired direction.
	 * <p>
	 * We use an approximation method here to be independent of the redirect model.
	 * With a good initialization only 1-2 iterations are required anyway
	 * </p>
	 *
	 * @param ballVel velocity of the ball when hitting the kicker
	 * @param shootSpeed velocity of the shot
	 * @param ballTargetDir desired direction of the ball after hit
	 * @param initialOrientation where to start with approximation, e.g. current bot (target) orientation
	 * @return target angle of the bot
	 */
	double calcTargetOrientation(final IVector2 ballVel, final double shootSpeed,
			final double ballTargetDir, final double initialOrientation)
	{
		return calcTargetOrientation(ballVel, shootSpeed, ballTargetDir, initialOrientation, frontLossFactor);
	}
	
	
	/**
	 * @param ballVel velocity of ball on hit
	 * @param collisionNormal normal vector of collision
	 * @return damped velocity
	 */
	double ballDamp(IVector2 ballVel, final IVector2 collisionNormal)
	{
		return ballDamp(ballVel, collisionNormal, frontLossFactor);
	}
	
	
	/**
	 * @param ballVel velocity of ball on hit
	 * @param collisionNormal normal vector of collision
	 * @param targetBallVel desired ball velocity after hit
	 * @return required kickSpeed in order to reach the desired targetBallVel after hit
	 */
	double calcKickSpeed(IVector2 ballVel, final IVector2 collisionNormal,
			final double targetBallVel)
	{
		return calcKickSpeed(ballVel, collisionNormal, targetBallVel, KICK_DAMP_FACTOR);
	}
	
}
