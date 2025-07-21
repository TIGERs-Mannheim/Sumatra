/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Common constants.
 * Note: Constants should preferably be defined where they are used.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OffensiveConstants
{
	@Getter
	@Configurable(defValue = "0.3")
	private static double minBotShouldDoGoalShotScore = 0.3;

	@Getter
	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation. Includes safety margin", defValue = "870.0")
	private static double maxDribblingLength = 870.0;

	@Getter
	@Configurable(defValue = "0.2", comment = "[m/s] Min ball vel to still receive balls via approachBallLine")
	private static double abortBallInterceptionVelThreshold = 0.2;

	@Getter
	@Configurable(defValue = "0.7", comment = "[m/s] Max ball vel to consider ball is received via approachBallLine")
	private static double ballIsRollingThreshold = 0.7;

	@Getter
	@Configurable(comment = "The maximum reasonable angle [rad] for redirects", defValue = "1.2")
	private static double maximumReasonableRedirectAngle = 1.2;

	@Getter
	@Configurable(comment = "Allow to try using overshooting trajectories while receiving", defValue = "true")
	private static boolean allowOvershootingBallInterceptions = true;

	@Getter
	@Configurable(defValue = "50", comment = "[mm] For chipped balls the ball must be below this height to be considered receivable")
	private static double maxInterceptHeight = 50;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveConstants.class);
	}
}
