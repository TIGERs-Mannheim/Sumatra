/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IRoleState;


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
	private final AMoveToSkill		skill;
											
											
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
		addEndTransition(EStateId.MOVING, EEvent.DONE);
		this.behavior = behavior;
		skill = AMoveToSkill.createMoveToSkill();
	}
	
	
	/**
	 * @param dest
	 * @param orientation
	 */
	public MoveRole(final IVector2 dest, final double orientation)
	{
		this(EMoveBehavior.DO_COMPLETE);
		skill.getMoveCon().updateDestination(dest);
		skill.getMoveCon().updateTargetAngle(orientation);
	}
	
	
	/**
	 * Moves the bot
	 * 
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private class MovingState implements IRoleState
	{
		
		
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
			if (skill.isDestinationReached() && (behavior == EMoveBehavior.DO_COMPLETE))
			{
				triggerEvent(EEvent.DONE);
			}
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.MOVING;
		}
		
	}
	
	
	/**
	 * @return
	 */
	public final MovementCon getMoveCon()
	{
		return skill.getMoveCon();
	}
	
	
	/**
	 * @return
	 */
	
	public final boolean isDestinationReached()
	{
		return skill.isDestinationReached();
	}
}
