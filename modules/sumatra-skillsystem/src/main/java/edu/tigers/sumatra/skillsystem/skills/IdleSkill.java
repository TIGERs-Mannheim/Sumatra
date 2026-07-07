package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Stops the bot and maintains update and feedback frequency
 */
public class IdleSkill extends AMoveSkill
{
	@Configurable(defValue = "true", comment = "true: Brake fast, false: Slow down smoothly", unit = EConfigUnit.BOOLEAN)
	private static boolean emergencyBrake = true;


	@Override
	public void doEntryActions()
	{
		if (emergencyBrake)
		{
			var moveConstraints = defaultMoveConstraints();
			moveConstraints = moveConstraints
					.withAccMax(moveConstraints.getBrkMax())
					.withAccMaxW(DriveLimits.MAX_ACC_W);
			setLocalVelocity(Vector2.zero(), 0, moveConstraints);
		} else
		{
			setMotorsOff();
		}
	}


	@Override
	public void doUpdate()
	{
		if (emergencyBrake && getVel().getLength2() < 0.1)
		{
			setMotorsOff();
		}
	}
}
