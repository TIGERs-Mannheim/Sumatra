/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.vector.Vector3f;


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
