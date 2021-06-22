/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.math.vector.VectorN;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


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
				.strictVelocityLimit(input.isStrictVelocityLimit())
				.build();
	}
}
