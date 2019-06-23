/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillWheelVelocitySim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillWheelVelocity skill = (BotSkillWheelVelocity) input.getSkill();
		
		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetWheelVel(VectorN.from(skill.getVelocities()))
				.modeXY(EDriveMode.WHEEL_VEL)
				.modeW(EDriveMode.WHEEL_VEL)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.addCommands(BotSkillSimulator.parseDataAcquisitionMode(input, skill.getDataAcquisitionMode()))
				.build();
	}
}
