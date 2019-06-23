/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillGlobalVelXyPosWSim implements IBotSkillSim
{
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillGlobalVelXyPosW skill = (BotSkillGlobalVelXyPosW) input.getSkill();
		
		IVector2 localVelXY = BotMath.convertGlobalBotVector2Local(skill.getVel(), input.getCurPos().z());
		
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
				.addCommands(BotSkillSimulator.parseDataAcquisitionMode(input, skill.getDataAcquisitionMode()))
				.build();
	}
}
