/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * This is a generic move role.
 * The only thing it does is to move according to moveCon.
 * So it considers your updateDestination and updateLookAtTarget.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public enum EMoveBehavior
	{
		/**  */
		NORMAL,
		/**  */
		LOOK_AT_BALL,
		/**  */
		DO_COMPLETE;
	}
	
	private enum EStateId
	{
		MOVING;
	}
	
	private enum EEvent
	{
		DONE,
		DEST_UPDATE
	}
	
	private final EMoveBehavior	behavior;
	private IMoveToSkill				skill;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Create a simple move role.
	 * 
	 * @param behavior
	 */
	public MoveRole(final EMoveBehavior behavior)
	{
		super(ERole.MOVE);
		IRoleState state = new MovingState();
		setInitialState(state);
		// addEndTransition(EStateId.MOVING, EEvent.DONE);
		this.behavior = behavior;
		skill = AMoveSkill.createMoveToSkill();
		skill.setDoComplete(behavior == EMoveBehavior.DO_COMPLETE);
	}
	
	
	/**
	 * @param dest
	 * @param orientation
	 * @param movingSpeed
	 * @param speed
	 */
	public MoveRole(final IVector2 dest, final float orientation, final EMovingSpeed movingSpeed, final float speed)
	{
		super(ERole.MOVE);
		IRoleState state = new MovingState();
		setInitialState(state);
		addEndTransition(EStateId.MOVING, EEvent.DONE);
		behavior = EMoveBehavior.DO_COMPLETE;
		skill = AMoveSkill.createMoveToSkill();
		skill.setDoComplete(true);
		skill.getMoveCon().updateDestination(dest);
		skill.getMoveCon().updateTargetAngle(orientation);
		skill.getMoveCon().setSpeed(movingSpeed, speed);
	}
	
	
	/**
	 * Moves the bot
	 * 
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private class MovingState implements IRoleState
	{
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.DONE);
		}
		
		
		@Override
		public void doEntryActions()
		{
			
			setNewSkill(skill);
			switch (behavior)
			{
				case LOOK_AT_BALL:
					skill.getMoveCon().updateLookAtTarget(getAiFrame().getWorldFrame().getBall());
					break;
				default:
					// nothing to do
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.MOVING;
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public final MovementCon getMoveCon()
	{
		return skill.getMoveCon();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		// only needed MOVING and this is already set in ARole
	}
	
	
	/**
	 * @return
	 */
	public boolean checkMoveCondition()
	{
		return getMoveCon().checkCondition(getWFrame(), getBotID()) == EConditionState.FULFILLED;
	}
}
