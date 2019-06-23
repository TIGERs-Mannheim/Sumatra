/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillFastGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillFastGlobalPositionSim implements IBotSkillSim
{
	private boolean	backwardOnly	= true;
	
	private boolean	fastPosMode;
	
	
	@Override
	public void init(final BotSkillInput input)
	{
		fastPosMode = false;
	}
	
	
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillFastGlobalPosition skill = (BotSkillFastGlobalPosition) input.getSkill();
		
		Vector3 targetPos = Vector3.from2d(skill.getPos(), 0);
		
		double distToTarget = input.getCurPos().getXYVector().distanceTo(skill.getPos()) * 1e-3;
		double trajVelAbs = input.getCurVelLocal().getLength2();
		
		EDriveMode driveModeZ;
		double localVelZ = 0;
		
		if ((distToTarget < 0.05) && (trajVelAbs < 0.5))
		{
			// almost at target position
			targetPos.set(2, skill.getOrientation());
			driveModeZ = EDriveMode.GLOBAL_POS;
			
			fastPosMode = false;
		} else
		{
			if (trajVelAbs < 0.01)
			{
				// orientation not stable over atan2 if too slow, keep orientation
				driveModeZ = EDriveMode.LOCAL_VEL;
			} else
			{
				IVector2 trajVelGlobal = BotMath.convertLocalBotVector2Global(input.getCurVelLocal().getXYVector(),
						input.getCurPos().z());
				
				double velOrient = Math.atan2(trajVelGlobal.y(), trajVelGlobal.x());
				double targetOrient = velOrient + Math.PI; // assume going backward first
				double orientDiff = AngleMath
						.normalizeAngle(targetOrient - AngleMath.normalizeAngle(input.getCurPos().z()));
				
				if ((Math.abs(orientDiff) > AngleMath.PI_HALF) && !backwardOnly)
				{
					// more oriented with the front in driving direction, go forward
					targetOrient -= Math.PI;
					targetOrient = AngleMath.normalizeAngle(targetOrient);
					orientDiff = AngleMath.normalizeAngle(targetOrient - AngleMath.normalizeAngle(input.getCurPos().z()));
				}
				
				if ((Math.abs(orientDiff) < 0.1) && !fastPosMode)
				{
					fastPosMode = true;
				}
				
				targetPos.set(2, targetOrient);
				driveModeZ = EDriveMode.GLOBAL_POS;
			}
		}
		
		MoveConstraints moveCon = skill.getMoveConstraints();
		
		if (fastPosMode)
		{
			moveCon.setAccMax(skill.getAccMaxFast());
		}
		
		return BotSkillOutput.Builder.create()
				.driveLimits(moveCon)
				.targetPos(targetPos)
				.targetVelLocal(Vector3.fromXYZ(0, 0, localVelZ))
				.modeXY(EDriveMode.GLOBAL_POS)
				.modeW(driveModeZ)
				.kickDevice(skill.getDevice())
				.kickMode(skill.getMode())
				.kickSpeed(skill.getKickSpeed())
				.dribblerRPM(skill.getDribbleSpeed())
				.strictVelocityLimit(input.isStrictVelocityLimit())
				.build();
	}
	
	
	public void setBackwardOnly(final boolean backwardOnly)
	{
		this.backwardOnly = backwardOnly;
	}
	
}
