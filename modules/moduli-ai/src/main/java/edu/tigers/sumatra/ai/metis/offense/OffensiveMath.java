/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;

import java.util.List;


/**
 * Common methods for the Offensive
 */
public final class OffensiveMath
{
	private static final int NUM_TOUCHDOWNS_FOR_PASSES = 4;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveMath.class);
	}


	private OffensiveMath()
	{
		// hide public constructor
	}


	public static boolean isAngleAccessible(List<AngleRange> unaccessibleAngles, double angleToCheck)
	{
		for (AngleRange range : unaccessibleAngles)
		{
			if (SumatraMath.isBetween(angleToCheck, range.getRightAngle(), range.getLeftAngle()))
			{
				return false;
			}
		}
		return true;
	}


	/**
	 * Get the redirect angle of a pass
	 *
	 * @param passSenderPos sender position, which most of the time is the ball position
	 * @param passReceiverPos receiving robot position
	 * @param passReceiverTarget redirecting target of the receiving robot.
	 * @return the absolute redirect angle [rad]
	 */
	public static double getRedirectAngle(
			final IVector2 passSenderPos,
			final IVector2 passReceiverPos,
			final IVector2 passReceiverTarget)
	{
		IVector2 botToBall = passSenderPos.subtractNew(passReceiverPos);
		IVector2 botToTarget = passReceiverTarget.subtractNew(passReceiverPos);
		return botToBall.angleToAbs(botToTarget).orElse(Math.PI);
	}


	private static boolean redirectAngleIsDoable(
			final IVector2 passSenderPos,
			final IVector2 passReceiverPos,
			final IVector2 passReceiverTarget)
	{
		IVector2 targetToReceiver = passReceiverPos.subtractNew(passReceiverTarget);
		IVector2 senderToReceiver = passReceiverPos.subtractNew(passSenderPos);

		double angleDeg = targetToReceiver.angleToAbs(senderToReceiver).orElse(0.0);
		return angleDeg < OffensiveConstants.getMaximumReasonableRedirectAngle();
	}


	public static double passEndVel(
			final IVector2 passSenderPos,
			final IVector2 passReceiverPos,
			final IVector2 passReceiverTarget)
	{
		IVector2 targetToReceiver = passReceiverPos.subtractNew(passReceiverTarget);
		IVector2 senderToReceiver = passReceiverPos.subtractNew(passSenderPos);

		if (redirectAngleIsDoable(passSenderPos, passReceiverPos, passReceiverTarget))
		{
			return adaptPassEndVelToRedirectAngle(targetToReceiver, senderToReceiver);
		}

		return OffensiveConstants.getMaxPassEndVelReceive();
	}


	private static double adaptPassEndVelToRedirectAngle(
			final IVector2 targetToReceiver,
			final IVector2 senderToReceiver)
	{
		double angleDeg = targetToReceiver.angleToAbs(senderToReceiver).orElse(0.0);
		double minAngle = OffensiveConstants.getMaxAngleForPassMaxSpeed();
		double maxAngle = OffensiveConstants.getMaxAngleForReducedSpeed();
		double relAngle = 1 - SumatraMath.relative(angleDeg, minAngle, maxAngle);

		double dynamicAngleRange = OffensiveConstants.getMaxPassEndVelRedirect()
				- OffensiveConstants.getMinPassEndVelRedirectReduction();
		return OffensiveConstants.getMinPassEndVelRedirectReduction() + relAngle * dynamicAngleRange;
	}


	/**
	 * The pass speed for passing from <code>passSenderPos</code> to <code>passReceiverPos</code>, considering
	 * the receivers next target to determine if a receive or redirect is required.
	 *
	 * @param passSenderPos sender position, which most of the time is the ball position
	 * @param passReceiverPos receiving robot position
	 * @param passReceiverTarget redirecting target of the receiving robot.
	 * @return the desired pass speed [m/s]
	 */
	public static double passSpeedStraight(
			final IVector2 passSenderPos,
			final IVector2 passReceiverPos,
			final IVector2 passReceiverTarget)
	{
		IStraightBallConsultant consultant = BallFactory.createStraightConsultant();

		double passDist = passSenderPos.distanceTo(passReceiverPos);
		double maxPassEndVel = passEndVel(passSenderPos, passReceiverPos, passReceiverTarget);
		double passSpeed = consultant.getInitVelForDist(passDist, maxPassEndVel);
		passSpeed = Math.min(RuleConstraints.getMaxBallSpeed(), passSpeed);
		passSpeed = Math.max(OffensiveConstants.getMinPassSpeed(), passSpeed);

		return reducePassSpeedAsRequired(consultant, passDist, passSpeed);
	}


	/**
	 * The pass speed for passing from <code>passSenderPos</code> to <code>passReceiverPos</code>, considering
	 * the receivers next target to determine if a receive or redirect is required.
	 *
	 * @param passSenderPos sender position, which most of the time is the ball position
	 * @param passReceiverPos receiving robot position
	 * @param maxPassEndVel desired pass end vel
	 * @return the desired pass speed [m/s]
	 */
	public static double passSpeedStraight(
			final IVector2 passSenderPos,
			final IVector2 passReceiverPos,
			final double maxPassEndVel)
	{
		IStraightBallConsultant consultant = BallFactory.createStraightConsultant();

		double passDist = passSenderPos.distanceTo(passReceiverPos);
		double passSpeed = consultant.getInitVelForDist(passDist, maxPassEndVel);
		passSpeed = Math.min(RuleConstraints.getMaxBallSpeed(), passSpeed);
		passSpeed = Math.max(OffensiveConstants.getMinPassSpeed(), passSpeed);

		return reducePassSpeedAsRequired(consultant, passDist, passSpeed);
	}


	/**
	 * The pass speed for chip kicks
	 *
	 * @param distance the desired distance of the pass
	 * @param maxChipSpeed
	 * @return a kick speed for which the pass will be receivable by the receiver
	 */
	public static double passSpeedChip(final double distance, final double maxChipSpeed)
	{
		double passSpeed = BallFactory.createChipConsultant().getInitVelForDistAtTouchdown(distance,
				NUM_TOUCHDOWNS_FOR_PASSES);
		return Math.min(maxChipSpeed, passSpeed);
	}


	private static double reducePassSpeedAsRequired(
			final IStraightBallConsultant consultant,
			final double passDist,
			final double desiredPassSpeed)
	{
		double minPassSpeed = OffensiveConstants.getMinPassSpeed();
		double minPassEndVel = OffensiveConstants.getMinPassEndVel();
		double minPassDuration = OffensiveConstants.getMinPassDuration();
		double passSpeed = desiredPassSpeed;
		for (double newPassSpeed = desiredPassSpeed; newPassSpeed > minPassSpeed; newPassSpeed -= 0.1)
		{
			double travelTime = consultant.getTimeForKick(passDist, newPassSpeed);
			double passEndVel = consultant.getVelForKickByTime(newPassSpeed, travelTime);
			if (passEndVel < minPassEndVel // passEndVel too low, can not reduce passSpeed anymore
					|| travelTime > minPassDuration // ball travels long enough
			)
			{
				break;
			}
			passSpeed = newPassSpeed;
		}
		return passSpeed;
	}


	/**
	 * Checks if a bot position is critical
	 *
	 * @param botPos position of the bot to check
	 * @param penArea position of the bot to check
	 * @return true if botpos is critical
	 */
	public static boolean isBotCritical(final IVector2 botPos, IPenaltyArea penArea)
	{
		return penArea.isPointInShape(botPos);
	}


	public static double passSpeedStraightKickInsBlaue(final IVector2 passOrigin, final IVector2 passTarget, double passEndVel)
	{
		IStraightBallConsultant consultant = BallFactory.createStraightConsultant();
		double passDist = passOrigin.distanceTo(passTarget);
		double passSpeed = consultant.getInitVelForDist(passDist, passEndVel);
		passSpeed = Math.min(RuleConstraints.getMaxBallSpeed(), passSpeed);
		passSpeed = Math.max(0.5, passSpeed);
		return passSpeed;
	}


	public static double passSpeedChipKickInsBlaue(final IVector2 passOrigin, final IVector2 passTarget, double maxChipSpeed)
	{
		double distance = passOrigin.distanceTo(passTarget);
		double passSpeed = BallFactory.createChipConsultant().getInitVelForDistAtTouchdown(distance,
				NUM_TOUCHDOWNS_FOR_PASSES);
		return Math.min(maxChipSpeed, passSpeed);
	}
}
