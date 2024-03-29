/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.BotSkillSine;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillSineSim implements IBotSkillSim
{
	private long tStart;
	
	
	@Override
	public void init(final BotSkillInput input)
	{
		tStart = input.tNow();
	}
	
	
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillSine skill = (BotSkillSine) input.skill();

		double freq = skill.getFrequency();

		double tNow = (input.tNow() - tStart) * 1e-9;

		double val = SumatraMath.sin(2.0 * Math.PI * tNow * freq);
		double valC = SumatraMath.cos(2.0 * Math.PI * tNow * freq);

		if (tNow < ((0.25) / freq))
		{
			valC = 0;
		}

		double velX = skill.getVel().x() * valC * 1000;
		double velY = skill.getVel().y() * val * 1000;
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
