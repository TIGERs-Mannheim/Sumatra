/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Common methods for the Offensive
 */
public final class OffensiveMath
{
	
	public static final int NUM_TOUCHDOWNS_FOR_PASSES = 4;
	
	
	private OffensiveMath()
	{
		// hide public constructor
	}
	
	
	public static boolean isKeeperInsane(final ITrackedBall ball, final GameState gameState)
	{
		return gameState.isStandardSituationForUs() && OffensiveConstants.isEnableInsanityMode()
				&& (ball.getPos().x() > ((Geometry.getFieldLength() / 2) - 250));
	}
	
	
	/**
	 * @param tacticalField the TF
	 * @return true -> Bot will shoot directly on the goal, false -> Bot will pass to another Bot
	 */
	public static boolean attackerCanScoreGoal(
			final ITacticalField tacticalField)
	{
		return tacticalField.getBestGoalKickTarget()
				.map(IRatedTarget::getScore).orElse(0.0) > OffensiveConstants.getMinDirectShotScore();
	}
	
	
	public static IBotIDMap<ITrackedBot> getPotentialOffensiveBotMap(
			final ITacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (newTacticalField.isOpponentWillDoIcing())
		{
			return new BotIDMap<>();
		}
		IBotIDMap<ITrackedBot> botMap = new BotIDMap<>();
		botMap.putAll(baseAiFrame.getWorldFrame().getTigerBotsAvailable());
		for (BotID key : newTacticalField.getCrucialDefender())
		{
			botMap.remove(key);
		}
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.ATTACKER)
				.stream().map(ARole::getBotID)
				.noneMatch(b -> b.equals(baseAiFrame.getKeeperId())))
		{
			// if insane keeper has performed a pass, allow it to stay attacker until pass is performed
			botMap.remove(baseAiFrame.getKeeperId());
		}
		for (BotID botID : newTacticalField.getBotInterchange().getDesiredInterchangeBots())
		{
			botMap.remove(botID);
		}
		return botMap;
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
	 * The pass speed for chip kicks
	 * 
	 * @param distance the desired distance of the pass
	 * @return a kick speed for which the pass will be receivable by the receiver
	 */
	public static double passSpeedChip(final double distance)
	{
		double passSpeed = BallFactory.createChipConsultant().getInitVelForDistAtTouchdown(distance,
				NUM_TOUCHDOWNS_FOR_PASSES);
		return Math.min(RuleConstraints.getMaxBallSpeed(), passSpeed);
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
}