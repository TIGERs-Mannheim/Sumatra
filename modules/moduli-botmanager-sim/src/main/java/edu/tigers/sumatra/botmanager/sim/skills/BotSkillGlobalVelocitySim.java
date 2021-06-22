/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalVelocity;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillGlobalVelocitySim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillGlobalVelocity skill = (BotSkillGlobalVelocity) input.getSkill();
		
		IVector2 localVelXY = BotMath.convertGlobalBotVector2Local(skill.getVel().getXYVector().multiplyNew(1e3),
				input.getState().getPose().getOrientation());
		
		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetVelLocal(Vector3.from2d(localVelXY, skill.getVel().z()))
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
