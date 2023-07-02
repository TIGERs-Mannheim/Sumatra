/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillGlobalPositionSim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillGlobalPosition skill = (BotSkillGlobalPosition) input.skill();
		
		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetPos(Vector3.from2d(skill.getPos(), skill.getOrientation()))
				.modeXY(EDriveMode.GLOBAL_POS)
				.modeW(EDriveMode.GLOBAL_POS)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.primaryDirection(skill.getPrimaryDirection())
				.strictVelocityLimit(input.strictVelocityLimit())
				.build();
	}
}
