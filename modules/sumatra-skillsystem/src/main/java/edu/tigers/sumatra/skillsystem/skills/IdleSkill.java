/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Stops the bot and maintains update and feedback frequency
 */
public class IdleSkill extends AMoveSkill
{
	@Configurable(defValue = "false", comment = "true: Brake fast, false: Slow down smoothly")
	private static boolean emergencyBrake = false;


	@Override
	public void doEntryActions()
	{
		if (emergencyBrake)
		{
			var moveConstraints = defaultMoveConstraints();
			moveConstraints.setAccMax(moveConstraints.getBrkMax());
			moveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
			moveConstraints.setJerkMax(DriveLimits.MAX_JERK);
			moveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);
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
