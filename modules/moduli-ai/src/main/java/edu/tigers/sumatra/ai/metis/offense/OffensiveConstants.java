/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


/**
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 */
public class OffensiveConstants
{
	@Configurable(comment = "time in seconds", defValue = "-2500.0")
	private static double minXPosForSupportiveAttacker = -2500;
	
	@Configurable(comment = "time in seconds", defValue = "3.0")
	private static double delayWaitTime = 3.0;
	
	@Configurable(comment = "no directShots when indirectFreeKick is called", defValue = "true")
	private static boolean forcePassWhenIndirectIsCalled = true;
	
	@Configurable(comment = "force pass in offensive Action", defValue = "false")
	private static boolean alwaysForcePass = false;
	
	@Configurable(comment = "min score to directly shot at goal", defValue = "0.4")
	private static double minDirectShotScore = 0.4;
	
	@Configurable(comment = "enable supportive Attacker", defValue = "true")
	private static boolean enableSupportiveAttacker = true;
	
	@Configurable(comment = "minimum time that a pass should take", defValue = "1.0")
	private static double minPassDuration = 1.0;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot for redirects", defValue = "3.0")
	private static double maxPassEndVelRedirect = 3.0;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot for receiving the ball", defValue = "4.0")
	private static double maxPassEndVelReceive = 4.0;
	
	@Configurable(comment = "Minimum ball velocity at kicker of receiving bot", defValue = "2.0")
	private static double minPassEndVelRedirectReduction = 2.0;
	
	@Configurable(comment = "Minimum ball velocity at kicker of receiving bot", defValue = "1.5")
	private static double minPassEndVel = 1.5;
	
	@Configurable(comment = "Minimum kick speed for passes", defValue = "2.0")
	private static double minPassSpeed = 2.0;
	
	@Configurable(defValue = "0.9", comment = "Passes with a redirect angle [rad] below this will use max passEndVel")
	private static double maxAngleForPassMaxSpeed = 0.9;
	
	@Configurable(defValue = "1.4", comment = "Passes with a redirect angle [rad] above this will use min passEndVel")
	private static double maxAngleForReducedSpeed = 1.4;
	
	@Configurable(comment = "The maximum reasonable angle [rad] for redirects", defValue = "1.4")
	private static double maximumReasonableRedirectAngle = 1.4;
	
	@Configurable(defValue = "3000.0")
	private static double chipKickMinDistToTarget = 3000;
	
	@Configurable(defValue = "true")
	private static boolean isInterceptorEnabled = true;
	
	@Configurable(comment = "Should keeper be allowed to leave the penalty area", defValue = "false")
	private static boolean enableInsanityMode = false;
	
	@Configurable(comment = "warning, this is storing large data in the tactical field!", defValue = "false")
	private static boolean enableOffensiveStatistics = false;
	
	@Configurable(defValue = "false")
	private static boolean enableNoSkirmishSupportiveAttacker = false;
	
	
	static
	{
		ConfigRegistration.registerClass("metis", OffensiveConstants.class);
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
	 * @return the delayWaitTime
	 */
	public static double getDelayWaitTime()
	{
		return delayWaitTime;
	}
	
	
	/**
	 * @return the forcePassWhenIndirectIsCalled
	 */
	public static boolean isForcePassWhenIndirectIsCalled()
	{
		return forcePassWhenIndirectIsCalled;
	}
	
	
	/**
	 * @return the enableSupportiveAttacker
	 */
	public static boolean isSupportiveAttackerEnabled()
	{
		return enableSupportiveAttacker;
	}
	
	
	/**
	 * @return the maxPassEndVelRedirect
	 */
	public static double getMaxPassEndVelRedirect()
	{
		return maxPassEndVelRedirect;
	}
	
	
	/**
	 * @return the minimum pass time
	 */
	public static double getMinPassDuration()
	{
		return minPassDuration;
	}
	
	
	/**
	 * @return the maxAngleForPassMaxSpeed
	 */
	public static double getMaxAngleForPassMaxSpeed()
	{
		return maxAngleForPassMaxSpeed;
	}
	
	
	/**
	 * @return the maxAngleForReducedSpeed
	 */
	public static double getMaxAngleForReducedSpeed()
	{
		return maxAngleForReducedSpeed;
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
	public static double getMaxPassEndVelReceive()
	{
		return maxPassEndVelReceive;
	}
	
	
	public static double getMinPassEndVel()
	{
		return minPassEndVel;
	}
	
	
	public static double getMinPassEndVelRedirectReduction()
	{
		return minPassEndVelRedirectReduction;
	}
	
	
	public static double getMinPassSpeed()
	{
		return minPassSpeed;
	}
	
	
	/**
	 * @return the maxPassEndVelReceive
	 */
	public static double getMaximumReasonableRedirectAngle()
	{
		return maximumReasonableRedirectAngle;
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
	
	
	public static double getMinXPosForSupportiveAttacker()
	{
		return minXPosForSupportiveAttacker;
	}
	
	
	public static double getMinDirectShotScore()
	{
		return minDirectShotScore;
	}
	
	
	public static boolean isAlwaysForcePass()
	{
		return alwaysForcePass;
	}
	
	
	public static boolean isEnableNoSkirmishSupportiveAttacker()
	{
		return enableNoSkirmishSupportiveAttacker;
	}
}
