/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillMotorsOffSim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		return BotSkillOutput.Builder.create()
				.driveLimits(input.getSkill().getMoveConstraints())
				.targetVelLocal(Vector3f.ZERO_VECTOR)
				.modeXY(EDriveMode.OFF)
				.modeW(EDriveMode.OFF)
				.kickDevice(EKickerDevice.STRAIGHT)
				.kickMode(EKickerMode.DISARM)
				.kickSpeed(0.0)
				.dribblerRPM(0.0)
				.build();
	}
}
