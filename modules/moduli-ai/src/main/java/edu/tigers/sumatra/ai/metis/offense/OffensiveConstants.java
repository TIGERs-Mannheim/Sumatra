/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.AngleMath;


/**
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 */
public class OffensiveConstants
{
	
	
	/**  */
	@Configurable(comment = "time in seconds")
	private static double	cheeringStopTimer										= 3.0;
	
	/**  */
	@Configurable(comment = "time in nanoseconds")
	private static double	delayWaitTime											= 3.0e9;
	
	/**  */
	@Configurable(comment = "show additional informations in debug log, lots of spamming")
	private static boolean	showDebugInformations								= false;
	
	/**  */
	@Configurable(comment = "no directShots when indirectFreeKick is called")
	private static boolean	forcePassWhenIndirectIsCalled						= true;
	
	/**  */
	@Configurable(comment = "minimal distance to ball for move Positions")
	private static double	minDistToBall											= 40;
	
	/**  */
	@Configurable(comment = "distance to our Penalty Area")
	private static double	distanceToPenaltyArea								= 300;
	
	
	/**  */
	@Configurable(comment = "finalKickStateDistance")
	private static double	finalKickStateDistance								= 650;
	
	/**  */
	@Configurable(comment = "finalKickStateUpdate")
	private static double	finalKickStateUpdate									= 300;
	
	/**  */
	@Configurable(comment = "chance to do hard coded plays for indirects in enemy half")
	private static double	chanceToDoSpecialMove								= 0.7;
	
	/**  */
	@Configurable(comment = "enable supportive Attacker")
	private static boolean	enableSupportiveAttacker							= true;
	
	@Configurable(comment = "max dist where ball can be pushed. if dist > this, then shoot")
	private static double	automatedThrowInPushDinstance						= 750;
	
	@Configurable(comment = "tolerance if ball is at proper position")
	private static double	automatedThrowInFinalTolerance					= 100;
	
	@Configurable(comment = "time in ns to wait for free path to target")
	private static double	automatedThrowInWaitForFreeSightTime			= 3e9;
	
	@Configurable(comment = "time in ns to clear from ball when it is placed at target")
	private static double	automatedThrowInClearMoveTime						= 1e9;
	
	@Configurable()
	private static double	interceptionSkillSecurityDist						= 150;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot")
	private static double	defaultPassEndVel										= 1.5;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot")
	private static double	defaultPassEndVelReceive							= 1.5;
	
	@Configurable(comment = "Added to kick speed due to ball not rolling after kick")
	private static double	kickSpeedOffset										= 1.0;
	
	@Configurable(comment = "Added time that the pass receiving robot needs to get to his passTarget")
	private static double	neededTimeForPassReceivingBotOffset				= 1.0;
	
	@Configurable()
	private static double	ballStartSpeedOffsetForPassTimeCalculation	= 1.0;
	
	@Configurable()
	private static double	maxAngleforPassMaxSpeed								= 50;
	
	@Configurable()
	private static double	maxAngleForReducedSpeed								= 100;
	
	@Configurable()
	private static double	passSpeedReductionForBadAngles					= 1.00;
	
	@Configurable(comment = "dont move when there is enough time")
	private static boolean	enableRedirectorStopMove							= false;
	
	@Configurable()
	private static long		chooseNewStrategyTimer								= 3_000_000_000L;
	
	@Configurable()
	private static double	acceptBestCatcherBallSpeedTreshold				= 0.8;
	
	@Configurable()
	private static double	desperateShotChipKickLength						= 2000;
	
	@Configurable(comment = "A passTarget is bad, when its rating is smaller than... this")
	private static double	classifyPassTargetAsBad								= 0.5;
	
	@Configurable(comment = "The maximum reasonable angle for redirects")
	private static double	maximumReasonableRedirectAngle					= AngleMath.deg2rad(90);
	
	@Configurable()
	private static double	minDistanceForSpeedAddition						= 3000;
	
	@Configurable()
	private static double	maxDistanceForSpeedAddition						= 9000;
	
	@Configurable()
	private static double	distanceSpeedAddition								= 2;
	
	@Configurable()
	private static double	chipKickCheckDistance								= 2000;
	
	@Configurable()
	private static double	chipKickMinDistToTarget								= 4000;
	
	@Configurable()
	private static boolean	isInterceptorEnabled									= true;
	
	@Configurable(comment = "Should keeper be allowed to leave the penalty area")
	private static boolean	enableInsanityMode									= false;
	
	static
	{
		ConfigRegistration.registerClass("offensive", OffensiveConstants.class);
	}
	
	
	/**
	 * @return the enableInsanityMode
	 */
	public static boolean isEnableInsanityMode()
	{
		return enableInsanityMode;
	}
	
	
	/**
	 * @return the automatedThrowInWaitForFreeSightTime
	 */
	public static double getAutomatedThrowInWaitForFreeSightTime()
	{
		return automatedThrowInWaitForFreeSightTime;
	}
	
	
	/**
	 * @return the cheeringStopTimer
	 */
	public static double getCheeringStopTimer()
	{
		return cheeringStopTimer;
	}
	
	
	/**
	 * @return the delayWaitTime
	 */
	public static double getDelayWaitTime()
	{
		return delayWaitTime;
	}
	
	
	/**
	 * @return the showDebugInformations
	 */
	public static boolean isShowDebugInformations()
	{
		return showDebugInformations;
	}
	
	
	/**
	 * @return the automatedThrowInPushDinstance
	 */
	public static double getAutomatedThrowInPushDinstance()
	{
		return automatedThrowInPushDinstance;
	}
	
	
	/**
	 * @return the automatedThrowInFinalTolerance
	 */
	public static double getAutomatedThrowInFinalTolerance()
	{
		return automatedThrowInFinalTolerance;
	}
	
	
	/**
	 * @return the forcePassWhenIndirectIsCalled
	 */
	public static boolean isForcePassWhenIndirectIsCalled()
	{
		return forcePassWhenIndirectIsCalled;
	}
	
	
	/**
	 * @return the minDistToBall
	 */
	public static double getMinDistToBall()
	{
		return minDistToBall;
	}
	
	
	/**
	 * @return the distanceToPenaltyArea
	 */
	public static double getDistanceToPenaltyArea()
	{
		return distanceToPenaltyArea;
	}
	
	
	/**
	 * @return the finalKickStateDistance
	 */
	public static double getFinalKickStateDistance()
	{
		return finalKickStateDistance;
	}
	
	
	/**
	 * @return the finalKickStateUpdate
	 */
	public static double getFinalKickStateUpdate()
	{
		return finalKickStateUpdate;
	}
	
	
	/**
	 * @return the chanceToDoSpecialMove
	 */
	public static double getChanceToDoSpecialMove()
	{
		return chanceToDoSpecialMove;
	}
	
	
	/**
	 * @return the enableSupportiveAttacker
	 */
	public static boolean isSupportiveAttackerEnabled()
	{
		return enableSupportiveAttacker;
	}
	
	
	/**
	 * @return the automatedThrowInClearMoveTime
	 */
	public static double getAutomatedThrowInClearMoveTime()
	{
		return automatedThrowInClearMoveTime;
	}
	
	
	/**
	 * @return the interceptionSkillSecurityDist
	 */
	public static double getInterceptionSkillSecurityDist()
	{
		return interceptionSkillSecurityDist;
	}
	
	
	/**
	 * @return the defaultPassEndVel
	 */
	public static double getDefaultPassEndVel()
	{
		return defaultPassEndVel;
	}
	
	
	/**
	 * @return the kickSpeedOffset
	 */
	public static double getKickSpeedOffset()
	{
		return kickSpeedOffset;
	}
	
	
	/**
	 * @return the neededTimeForPassReceivingBotOffset
	 */
	public static double getNeededTimeForPassReceivingBotOffset()
	{
		return neededTimeForPassReceivingBotOffset;
	}
	
	
	/**
	 * @return the ballStartSpeedOffsetForPassTimeCalculation
	 */
	public static double getBallStartSpeedOffsetForPassTimeCalculation()
	{
		return ballStartSpeedOffsetForPassTimeCalculation;
	}
	
	
	/**
	 * @return the maxAngleforPassMaxSpeed
	 */
	public static double getMaxAngleforPassMaxSpeed()
	{
		return maxAngleforPassMaxSpeed;
	}
	
	
	/**
	 * @return the maxAngleForReducedSpeed
	 */
	public static double getMaxAngleForReducedSpeed()
	{
		return maxAngleForReducedSpeed;
	}
	
	
	/**
	 * @return the passSpeedReductionForBadAngles
	 */
	public static double getPassSpeedReductionForBadAngles()
	{
		return passSpeedReductionForBadAngles;
	}
	
	
	/**
	 * @return the enableRedirectorStopMove
	 */
	public static boolean isEnableRedirectorStopMove()
	{
		return enableRedirectorStopMove;
	}
	
	
	/**
	 * @return the chipKickMinDistToTarget
	 */
	public static double getChipKickMinDistToTarget()
	{
		return chipKickMinDistToTarget;
	}
	
	
	/**
	 * @return the minDistanceForSpeedAddition
	 */
	public static double getMinDistanceForSpeedAddition()
	{
		return minDistanceForSpeedAddition;
	}
	
	
	/**
	 * @return the maxDistanceForSpeedAddition
	 */
	public static double getMaxDistanceForSpeedAddition()
	{
		return maxDistanceForSpeedAddition;
	}
	
	
	/**
	 * @return the distanceSpeedAddition
	 */
	public static double getDistanceSpeedAddition()
	{
		return distanceSpeedAddition;
	}
	
	
	/**
	 * @return the chooseNewStrategyTimer
	 */
	public static long getChooseNewStrategyTimer()
	{
		return chooseNewStrategyTimer;
	}
	
	
	/**
	 * @return the acceptBestCatcherBallSpeedTreshold
	 */
	public static double getAcceptBestCatcherBallSpeedTreshold()
	{
		return acceptBestCatcherBallSpeedTreshold;
	}
	
	
	/**
	 * @return the desperateShotChipKickLength
	 */
	public static double getDesperateShotChipKickLength()
	{
		return desperateShotChipKickLength;
	}
	
	
	/**
	 * @return the classifyPassTargetAsBad
	 */
	public static double getClassifyPassTargetAsBad()
	{
		return classifyPassTargetAsBad;
	}
	
	
	/**
	 * @return default end Vel
	 */
	public static double getDefaultPassEndVelReceive()
	{
		return defaultPassEndVelReceive;
	}
	
	
	/**
	 * @return the defaultPassEndVelReceive
	 */
	public static double getMaximumReasonableRedirectAngle()
	{
		return maximumReasonableRedirectAngle;
	}
	
	
	/**
	 * @return the chipKickCheckDistance
	 */
	public static double getChipKickCheckDistance()
	{
		return chipKickCheckDistance;
	}
	
	
	/**
	 * @return the isInterceptorEnabled
	 */
	public static boolean isInterceptorEnabled()
	{
		return isInterceptorEnabled;
	}
	
}
