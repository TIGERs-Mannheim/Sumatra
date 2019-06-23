/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Simple shooter role, basically to test skill
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimpleShooterRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  */
	public SimpleShooterRole()
	{
		super(ERole.SIMPLE_SHOOTER);
		IRoleState state1 = new PrepareState();
		
		setInitialState(state1);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, new ShootState());
		addTransition(EStateId.SHOOT, EEvent.LOST_BALL, state1);
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		SHOOT
	}
	
	private enum EEvent
	{
		PREPARED,
		LOST_BALL
	}
	
	private class PrepareState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			IVector2 dest = GeoMath.stepAlongLine(getWFrame().getBall().getPos(), getShootTarget(), -200);
			skill.getMoveCon().updateDestination(dest);
			skill.getMoveCon().updateLookAtTarget(getWFrame().ball);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			setCompleted();
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class ShootState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			// setNewSkill(new ShooterBotSkill(getShootTarget(), EKickDevice.STRAIGHT, 8000));
			setNewSkill(new KickSkill(new DynamicPosition(getShootTarget()), EKickMode.MAX));
		}
		
		
		@Override
		public void doUpdate()
		{
			// float dist = GeoMath.distancePP(getPos(), getWFrame().getBall().getPos());
			// if (dist > 1000)
			// {
			// nextState(EEvent.LOST_BALL);
			// }
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			nextState(EEvent.LOST_BALL);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.SHOOT;
		}
	}
	
	
	private IVector2 getShootTarget()
	{
		IVector2 target;
		if (getAiFrame().getTacticalField().getBestDirectShootTarget() != null)
		{
			target = getAiFrame().getTacticalField().getBestDirectShootTarget();
		} else
		{
			target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		}
		return target;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
