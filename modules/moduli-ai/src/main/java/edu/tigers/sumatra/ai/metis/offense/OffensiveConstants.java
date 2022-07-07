/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Common constants.
 * Note: Constants should preferably be defined where they are used.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OffensiveConstants
{
	@Configurable(defValue = "0.3")
	private static double minBotShouldDoGoalShotScore = 0.3;

	@Configurable(comment = "The maximum reasonable angle [rad] for redirects", defValue = "1.2")
	private static double maximumReasonableRedirectAngle = 1.2;

	@Configurable(comment = "warning, this is storing large data in the tactical field!", defValue = "false")
	private static boolean enableOffensiveStatistics = false;

	@Configurable(comment = "Ball speed at target", defValue = "1.8")
	private static double ballSpeedAtTargetKickInsBlaue = 1.8;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveConstants.class);
	}


	public static double getMaximumReasonableRedirectAngle()
	{
		return maximumReasonableRedirectAngle;
	}


	public static boolean isEnableOffensiveStatistics()
	{
		return enableOffensiveStatistics;
	}


	public static double getMinBotShouldDoGoalShotScore()
	{
		return minBotShouldDoGoalShotScore;
	}


	public static double getBallSpeedAtTargetKickInsBlaue()
	{
		return ballSpeedAtTargetKickInsBlaue;
	}
}
