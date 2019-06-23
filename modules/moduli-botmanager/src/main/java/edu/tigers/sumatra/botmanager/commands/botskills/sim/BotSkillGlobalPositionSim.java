/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillGlobalPositionSim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillGlobalPosition pos = (BotSkillGlobalPosition) input.getSkill();
		
		return BotSkillOutput.Builder.create()
				.driveLimits(pos.getMoveConstraints())
				.targetPos(Vector3.from2d(pos.getPos(), pos.getOrientation()))
				.modeXY(EDriveMode.GLOBAL_POS)
				.modeW(EDriveMode.GLOBAL_POS)
				.kickDevice(pos.getDevice())
				.kickMode(pos.getMode())
				.kickSpeed(pos.getKickSpeed())
				.dribblerRPM(pos.getDribbleSpeed())
				.primaryDirection(pos.getPrimaryDirection())
				.strictVelocityLimit(input.isStrictVelocityLimit())
				.build();
	}
}
