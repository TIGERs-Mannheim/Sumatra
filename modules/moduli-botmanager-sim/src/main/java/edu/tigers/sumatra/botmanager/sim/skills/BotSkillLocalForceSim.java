/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalForce;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


public class BotSkillLocalForceSim implements IBotSkillSim
{

	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		// By now only for spinTest
		BotSkillLocalForce skill = (BotSkillLocalForce) input.getSkill();

		final double fraction = 0.2;
		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetVelLocal(Vector3.fromXYZ(skill.getX(), skill.getY(), skill.getW() - fraction))
				.modeXY(EDriveMode.LOCAL_VEL)
				.modeW(EDriveMode.LOCAL_VEL)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.strictVelocityLimit(input.isStrictVelocityLimit())
				.build();
	}
}
