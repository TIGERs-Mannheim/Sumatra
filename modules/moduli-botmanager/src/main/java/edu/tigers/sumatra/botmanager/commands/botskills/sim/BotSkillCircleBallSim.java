/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillCircleBall;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillCircleBallSim implements IBotSkillSim
{
	private boolean atTargetAngle;
	
	
	@Override
	public void init(final BotSkillInput input)
	{
		atTargetAngle = false;
	}
	
	
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillCircleBall skill = (BotSkillCircleBall) input.getSkill();
		
		double radius = skill.getRadius() * 0.001;
		double speed = skill.getSpeed();
		double targetAngle = skill.getTargetAngle();
		double mu = skill.getFriction();
		
		double curSpeedAbs = input.getCurVelLocal().getLength2();
		
		IVector3 localVel;
		EKickerMode kickerMode = EKickerMode.DISARM;
		
		if (Math.abs(radius) < 0.01)
		{
			localVel = Vector3f.ZERO_VECTOR;
		} else
		{
			double theta = Math.atan(((curSpeedAbs * curSpeedAbs) / radius) * 9.81 * mu);
			
			double angDiff = AngleMath.normalizeAngle(targetAngle - input.getCurPos().z());
			if (Math.abs(angDiff) < 0.01)
			{
				atTargetAngle = true;
			}
			
			if (atTargetAngle && (skill.getMode() != EKickerMode.DISARM))
			{
				kickerMode = skill.getMode();
			}
			
			localVel = Vector3.fromXYZ(SumatraMath.sin(theta) * speed, SumatraMath.cos(-theta) * speed,
					curSpeedAbs / radius);
		}
		
		return BotSkillOutput.Builder.create()
				.driveLimits(skill.getMoveConstraints())
				.targetVelLocal(localVel)
				.modeXY(EDriveMode.LOCAL_VEL)
				.modeW(EDriveMode.LOCAL_VEL)
				.kickDevice(skill.getDevice())
				.kickMode(kickerMode)
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.build();
	}
}
