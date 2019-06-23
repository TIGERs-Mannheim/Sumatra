/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Simple shooter role, basically to test skill
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimpleShooterRole extends ARole
{
	private DynamicPosition passTarget;
	
	
	/**
	 * @param passTarget
	 */
	public SimpleShooterRole(final DynamicPosition passTarget)
	{
		super(ERole.SIMPLE_SHOOTER);
		setPassTarget(passTarget);
		setInitialState(new ShootState());
	}
	
	
	private enum EStateId
	{
		PREPARE,
		SHOOT
	}
	
	
	private class ShootState implements IRoleState
	{
		private KickSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new KickSkill(passTarget);
			skill.setMoveMode(EMoveMode.CHILL);
			skill.setKickMode(EKickMode.FIXED_SPEED);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.setReceiver(passTarget);
			getAiFrame().getAICom().setOffensiveRolePassTarget(passTarget);
			getAiFrame().getAICom().setOffensiveRolePassTargetID((BotID) passTarget.getTrackedId());
			
			double passVel = OffensiveMath.calcPassSpeedForReceivers(getPos(), passTarget,
					getAiFrame().getTacticalField().getBestDirectShotTargetsForTigerBots().get(passTarget.getTrackedId()));
			skill.setKickSpeed(passVel);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.SHOOT;
		}
	}
	
	
	/**
	 * Set the Target where to pass to.
	 * 
	 * @param passTarget
	 */
	public void setPassTarget(final DynamicPosition passTarget)
	{
		this.passTarget = passTarget;
	}
}
