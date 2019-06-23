/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * This role is able to chip kick the ball in a specified distance and way.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ChipKickRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2	target;
	private final float		rollDistance;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Default constructor
	 */
	public ChipKickRole()
	{
		this(Vector2.ZERO_VECTOR, 0);
	}
	
	
	/**
	 * @param target where ball will touch the ground the first time
	 * @param rollDistance distance to roll after ball touched the ground (may be negative)
	 */
	public ChipKickRole(IVector2 target, float rollDistance)
	{
		super(ERole.CHIP_KICK);
		setInitialState(new GetState());
		addTransition(EStateId.GET, EEvent.PREPARED, new ChipState());
		addEndTransition(EStateId.CHIP, EEvent.CHIPPED);
		this.target = target;
		this.rollDistance = rollDistance;
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		CHIP,
		GET
	}
	
	private enum EEvent
	{
		PREPARED,
		CHIPPED
	}
	
	private class GetState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			updateDestination(GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), target,
					-AIConfig.getBotConfig(getBotType()).getGeneral().getPositioningPreAiming()));
			updateLookAtTarget(target);
			setNewSkill(new MoveToSkill(getMoveCon()));
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
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.PREPARED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET;
		}
	}
	
	private class ChipState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new ChipAutoSkill(target, rollDistance));
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
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			setCompleted();
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.CHIP;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.CHIP_KICKER);
		features.add(EFeature.DRIBBLER);
	}
}
