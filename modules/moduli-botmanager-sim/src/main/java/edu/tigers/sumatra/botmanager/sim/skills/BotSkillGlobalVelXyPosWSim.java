/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillGlobalVelXyPosWSim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillGlobalVelXyPosW skill = (BotSkillGlobalVelXyPosW) input.skill();

		IVector2 localVelXY = BotMath.convertGlobalBotVector2Local(skill.getVel().multiplyNew(1e3),
				input.state().getPose().getOrientation());

		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetVelLocal(Vector3.from2d(localVelXY, 0))
				.targetPos(Vector3.fromXYZ(0, 0, skill.getTargetAngle()))
				.modeXY(EDriveMode.LOCAL_VEL)
				.modeW(EDriveMode.GLOBAL_POS)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.strictVelocityLimit(input.broadcast().isStrictVelocityLimit())
				.build();
	}
}
