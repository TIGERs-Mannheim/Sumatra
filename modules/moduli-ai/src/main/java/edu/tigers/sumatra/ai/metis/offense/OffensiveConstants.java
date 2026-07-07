package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
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
	@Configurable(defValue = "0.3", unit = EConfigUnit.PERCENTAGE)
	private static double minBotShouldDoGoalShotScore = 0.3;

	@Getter
	@Configurable(comment = "Any dribbling distance above this value is considered a violation. Includes safety margin", defValue = "870.0", unit = EConfigUnit.DISTANCE_MM)
	private static double maxDribblingLength = 870.0;

	@Getter
	@Configurable(defValue = "0.2", comment = "Min ball vel to still receive balls via approachBallLine", unit = EConfigUnit.VELOCITY_M)
	private static double abortBallInterceptionVelThreshold = 0.2;

	@Getter
	@Configurable(defValue = "0.7", comment = "Max ball vel to consider ball is received via approachBallLine", unit = EConfigUnit.VELOCITY_M)
	private static double ballIsRollingThreshold = 0.7;

	@Getter
	@Configurable(comment = "The maximum reasonable angle for redirects", defValue = "1.2", unit = EConfigUnit.ANGLE_RAD)
	private static double maximumReasonableRedirectAngle = 1.2;

	@Getter
	@Configurable(comment = "Allow to try using overshooting trajectories and emergency breaks while receiving", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean allowTrajectoryTricksWhileReceiving = true;

	@Getter
	@Configurable(comment = "The maximum planned velocity the robot may drive while receiving the ball", defValue = "1.5", unit = EConfigUnit.VELOCITY_M)
	private static double maxPlannedBotReceiveVelocity = 1.5;

	@Getter
	@Configurable(defValue = "50", comment = "For chipped balls the ball must be below this height to be considered receivable", unit = EConfigUnit.DISTANCE_MM)
	private static double maxInterceptHeight = 50;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveConstants.class);
	}
}
