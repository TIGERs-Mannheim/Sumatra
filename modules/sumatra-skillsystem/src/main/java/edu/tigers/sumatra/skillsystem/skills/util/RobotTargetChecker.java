package edu.tigers.sumatra.skillsystem.skills.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.skillsystem.data.RobotTarget;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;
import lombok.experimental.Accessors;


@Accessors(chain = true)
public class RobotTargetChecker
{
	@Configurable(defValue = "10.0", comment = "Position tolerance for skill state", unit = EConfigUnit.DISTANCE_MM)
	private static double positionTolerance = 10;

	@Configurable(defValue = "0.1", comment = "Orientation tolerance for skill state", unit = EConfigUnit.ANGLE_RAD)
	private static double orientationTolerance = 0.1;

	static
	{
		ConfigRegistration.registerClass("skills", RobotTargetChecker.class);
	}

	@Setter
	private double minTimeAtDest;

	private long tReached;


	public boolean isTargetReached(
			WorldFrame wFrame,
			BotID botId,
			RobotTarget target
	)
	{
		var botPos = wFrame.getBot(botId).getPos();
		var botOrientation = wFrame.getBot(botId).getOrientation();
		var now = wFrame.getTimestamp();

		var destination = target.getDestination(wFrame, botId);
		var targetAngle = target.getTargetAngle(wFrame, botId);
		var destReached = destination.distanceTo(botPos) < positionTolerance;
		var angleReached = AngleMath.diffAbs(targetAngle, botOrientation) < orientationTolerance;

		if (!destReached || !angleReached)
		{
			tReached = 0;
			return false;
		}
		if (tReached == 0)
		{
			tReached = now;
		}

		return (now - tReached) / 1e9 >= minTimeAtDest;
	}
}
