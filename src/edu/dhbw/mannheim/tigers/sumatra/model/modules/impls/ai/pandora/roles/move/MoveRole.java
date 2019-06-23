/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * This is a generic move role.
 * The only thing it does is to move according to moveCon.
 * So it considers your updateDestination and updateLookAtTarget.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
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
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor
	 */
	@Deprecated
	public MoveRole()
	{
		this(EMoveBehavior.LOOK_AT_BALL);
		updateDestination(Vector2.ZERO_VECTOR);
		updateLookAtTarget(AIConfig.getGeometry().getPenaltyMarkTheir());
	}
	
	
	/**
	 * Create a simple move role.
	 * @param behavior
	 */
	public MoveRole(EMoveBehavior behavior)
	{
		super(ERole.MOVE);
		IRoleState state = new MovingState();
		setInitialState(state);
		// addTransition(EStateId.MOVING, EEvent.DEST_UPDATE, state);
		addEndTransition(EStateId.MOVING, EEvent.DONE);
		this.behavior = behavior;
	}
	
	
	/**
	 * Moves the bot
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 * 
	 */
	private class MovingState implements IRoleState
	{
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.DONE);
		}
		
		
		@Override
		public void doEntryActions()
		{
			if (behavior == EMoveBehavior.DO_COMPLETE)
			{
				setNewSkill(new MoveToSkill(getMoveCon()));
			} else
			{
				setNewSkill(new MoveAndStaySkill(getMoveCon()));
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			switch (behavior)
			{
				case LOOK_AT_BALL:
					updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
					break;
				default:
					// nothing to do
			}
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
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		// only needed MOVING and this is already set in ARole
	}
}
