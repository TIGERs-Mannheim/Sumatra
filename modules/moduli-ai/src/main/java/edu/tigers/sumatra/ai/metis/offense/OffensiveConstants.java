/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
	@Configurable(comment = "time in seconds", defValue = "3.0")
	private static double cheeringStopTimer = 3.0;
	
	@Configurable(comment = "time in seconds", defValue = "-2500.0")
	private static double minXPosForSupportiveAttacker = -2500;
	
	/**  */
	@Configurable(comment = "time in nanoseconds", defValue = "3.0e9")
	private static double delayWaitTime = 3.0e9;
	
	@Configurable(comment = "if ball is NOT coming towards me, switch redirect to redirect if ball vel bigger this", defValue = "2.0")
	private static double minBallVelForSwitchToRedirect = 2.0;
	
	/**  */
	@Configurable(comment = "show additional informations in debug log, lots of spamming", defValue = "false")
	private static boolean showDebugInformations = false;
	
	/**  */
	@Configurable(comment = "allow redirector to overtake opponent infront of him", defValue = "false")
	private static boolean allowRedirectorOvertake = false;
	
	/**  */
	@Configurable(comment = "no directShots when indirectFreeKick is called", defValue = "true")
	private static boolean forcePassWhenIndirectIsCalled = true;
	
	/**  */
	@Configurable(comment = "force pass in offensive Action", defValue = "false")
	private static boolean alwaysForcePass = false;
	
	/** */
	@Configurable(comment = "min score to directly shot at goal", defValue = "0.4")
	private static double minDirectShotScore = 0.4;
	
	/**  */
	@Configurable(comment = "distance to our Penalty Area", defValue = "300.0")
	private static double distanceToPenaltyArea = 300;
	
	/**  */
	@Configurable(comment = "finalKickStateDistance", defValue = "650.0")
	private static double finalKickStateDistance = 650;
	/**  */
	@Configurable(comment = "enable supportive Attacker", defValue = "true")
	private static boolean enableSupportiveAttacker = true;
	
	@Configurable(comment = "enable protection mode and push around obstacle mode", defValue = "true")
	private static boolean enableProtectionMode = true;
	
	@Configurable(comment = "max dist where ball can be pushed. if dist > this, then shoot", defValue = "750.0")
	private static double automatedThrowInPushDistance = 750;
	
	@Configurable(comment = "tolerance if ball is at proper position", defValue = "100.0")
	private static double automatedThrowInFinalTolerance = 100;
	
	@Configurable(comment = "time in ns to clear from ball when it is placed at target", defValue = "1e9")
	private static double automatedThrowInClearMoveTime = 1e9;
	
	@Configurable(comment = "minimum time that a pass should take", defValue = "1.0")
	private static double minPassTime = 1.0;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot", defValue = "3.5")
	private static double defaultPassEndVel = 3.5;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot", defValue = "4.5")
	private static double defaultPassEndVelReceive = 4.5;
	
	@Configurable(comment = "Added time that the pass receiving robot needs to get to his passTarget", defValue = "1.0")
	private static double neededTimeForPassReceivingBotOffset = 1.0;
	
	@Configurable(defValue = "50.0")
	private static double maxAngleforPassMaxSpeed = 50;
	
	@Configurable(defValue = "100.0")
	private static double maxAngleForReducedSpeed = 100;
	
	@Configurable(defValue = "1.00")
	private static double passSpeedReductionForBadAngles = 1.00;
	
	@Configurable(comment = "dont move when there is enough time", defValue = "false")
	private static boolean enableRedirectorStopMove = false;
	
	@Configurable(comment = "The maximum reasonable angle for redirects", defValue = "1.4")
	private static double maximumReasonableRedirectAngle = AngleMath.deg2rad(90);
	
	@Configurable(defValue = "2000.0")
	private static double chipKickCheckDistance = 2000;
	
	@Configurable(defValue = "2000.0")
	private static double chipKickMinDistToTarget = 2000;
	
	@Configurable(defValue = "true")
	private static boolean isInterceptorEnabled = true;
	
	@Configurable(comment = "Should keeper be allowed to leave the penalty area", defValue = "false")
	private static boolean enableInsanityMode = false;
	
	@Configurable(comment = "warning, this is storing large data in the tactical field!", defValue = "false")
	private static boolean enableOffensiveStatistics = false;
	
	@Configurable(comment = "When score chance greater than this shoot instead of redirect", defValue = "0.18")
	private static double minScoreChanceShootInsteadRedirect = 0.18;
	
	@Configurable(comment = "use a (experimental) beta distribution for redirects", defValue = "true")
	private static boolean useBetaDistributionForRedirects = true;
	
	@Configurable(defValue = "true")
	private static boolean isSmartDistanceCalcAllowedForInterceptor = true;
	
	@Configurable(defValue = "true")
	private static boolean enableNoSkirmishSupportiveAttacker = true;
	
	
	static
	{
		ConfigRegistration.registerClass("offensive", OffensiveConstants.class);
	}
	
	
	private OffensiveConstants()
	{
		// hide public constructor
	}
	
	
	/**
	 * @return the enableInsanityMode
	 */
	public static boolean isEnableInsanityMode()
	{
		return enableInsanityMode;
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
	 * @return the automatedThrowInPushDistance
	 */
	public static double getAutomatedThrowInPushDistance()
	{
		return automatedThrowInPushDistance;
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
	 * @return the defaultPassEndVel
	 */
	public static double getDefaultPassEndVel()
	{
		return defaultPassEndVel;
	}
	
	
	/**
	 * @return the minimum pass time
	 */
	public static double getMinPassTime()
	{
		return minPassTime;
	}
	
	
	/**
	 * @return the neededTimeForPassReceivingBotOffset
	 */
	public static double getNeededTimeForPassReceivingBotOffset()
	{
		return neededTimeForPassReceivingBotOffset;
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
	
	
	/**
	 * @return are the statisctics enabled
	 */
	public static boolean isEnableOffensiveStatistics()
	{
		return enableOffensiveStatistics;
	}
	
	
	public static boolean isEnableProtectionMode()
	{
		return enableProtectionMode;
	}
	
	
	public static double getMinXPosForSupportiveAttacker()
	{
		return minXPosForSupportiveAttacker;
	}
	
	
	public static boolean isAllowRedirectorOvertake()
	{
		return allowRedirectorOvertake;
	}
	
	
	public static double getMinDirectShotScore()
	{
		return minDirectShotScore;
	}
	
	
	public static double getMinBallVelForSwitchToRedirect()
	{
		return minBallVelForSwitchToRedirect;
	}
	
	
	public static boolean isAlwaysForcePass()
	{
		return alwaysForcePass;
	}
	
	
	public static double getMinScoreChanceShootInsteadRedirect()
	{
		return minScoreChanceShootInsteadRedirect;
	}
	
	
	/**
	 * @return
	 */
	public static boolean useBetaDistributionForRedirects()
	{
		return useBetaDistributionForRedirects;
	}
	
	
	public static boolean isIsSmartDistanceCalcAllowedForInterceptor()
	{
		return isSmartDistanceCalcAllowedForInterceptor;
	}
	
	
	public static boolean isEnableNoSkirmishSupportiveAttacker()
	{
		return enableNoSkirmishSupportiveAttacker;
	}
}
