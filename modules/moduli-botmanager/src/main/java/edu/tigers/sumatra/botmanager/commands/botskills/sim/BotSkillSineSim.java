/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillSine;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillSineSim implements IBotSkillSim
{
	private long tStart;
	
	
	@Override
	public void init(final BotSkillInput input)
	{
		tStart = input.gettNow();
	}
	
	
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillSine skill = (BotSkillSine) input.getSkill();
		
		double freq = skill.getFrequency();
		
		double tNow = (input.gettNow() - tStart) * 1e-9;
		
		double val = SumatraMath.sin(2.0 * Math.PI * tNow * freq);
		double valC = SumatraMath.cos(2.0 * Math.PI * tNow * freq);
		
		if (tNow < ((0.25 * 1.0) / freq))
		{
			valC = 0;
		}
		
		double velX = skill.getVel().x() * valC;
		double velY = skill.getVel().y() * val;
		double velW = skill.getVel().z() * val;
		
		MoveConstraints moveCon = skill.getMoveConstraints();
		moveCon.setAccMax(DriveLimits.MAX_ACC);
		moveCon.setAccMaxW(DriveLimits.MAX_ACC_W);
		moveCon.setJerkMax(DriveLimits.MAX_JERK);
		moveCon.setJerkMaxW(DriveLimits.MAX_JERK_W);
		
		return BotSkillOutput.Builder.create()
				.driveLimits(moveCon)
				.targetVelLocal(Vector3.fromXYZ(velX, velY, velW))
				.modeXY(EDriveMode.LOCAL_VEL)
				.modeW(EDriveMode.LOCAL_VEL)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.build();
	}
}
