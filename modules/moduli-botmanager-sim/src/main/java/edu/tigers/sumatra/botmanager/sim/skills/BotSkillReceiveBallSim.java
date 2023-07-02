/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillReceiveBall;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


public class BotSkillReceiveBallSim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(BotSkillInput input)
	{
		var skill = (BotSkillReceiveBall) input.skill();
		var dest = BotShape.getCenterFromKickerPos(skill.getReceivePos(), skill.getReceiveOrientation(),
				input.botParams().getDimensions().getCenter2DribblerDist());

		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetPos(Vector3.from2d(dest, skill.getReceiveOrientation()))
				.modeXY(EDriveMode.GLOBAL_POS)
				.modeW(EDriveMode.GLOBAL_POS)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.primaryDirection(null)
				.strictVelocityLimit(input.strictVelocityLimit())
				.build();
	}
}
