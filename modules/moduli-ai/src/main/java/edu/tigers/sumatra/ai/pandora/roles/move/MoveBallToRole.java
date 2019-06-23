/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 14, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.move;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Move the ball to a target.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class MoveBallToRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final double	BALL_REACHED_TARGET_TOL	= 100;
	
	private final IVector2			target;
	
	@Configurable(comment = "Dist [mm] - distance to ball before aiming")
	private static double			positioningPreAiming		= 150;
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		GET,
		PUSH,
		DONE,
		GET_BEFORE,
		AIM;
	}
	
	private enum EEvent
	{
		PREPARED,
		AIMED,
		BALL_GOT,
		BALL_POSITIONED,
		BALL_LOST;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor
	 */
	@SuppressWarnings("unused")
	private MoveBallToRole()
	{
		this(AVector2.ZERO_VECTOR);
	}
	
	
	/**
	 * @param target
	 */
	public MoveBallToRole(final IVector2 target)
	{
		super(ERole.MOVE_BALL_TO);
		this.target = target;
		GetBeforeState getBeforeState = new GetBeforeState();
		PushState pushState = new PushState();
		AimState aimState = new AimState();
		addTransition(EStateId.GET_BEFORE, EEvent.PREPARED, aimState);
		addTransition(EStateId.AIM, EEvent.AIMED, pushState);
		addTransition(EStateId.PUSH, EEvent.BALL_LOST, getBeforeState);
		addEndTransition(EEvent.BALL_POSITIONED);
		setInitialState(getBeforeState);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class GetBeforeState implements IRoleState
	{
		private AMoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(
					GeoMath.stepAlongLine(getAiFrame().getWorldFrame().getBall().getPos(), target,
							-positioningPreAiming - 100));
			skill.getMoveCon().updateLookAtTarget(target);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().updateDestination(
					GeoMath.stepAlongLine(getAiFrame().getWorldFrame().getBall().getPos(), target,
							-positioningPreAiming - 100));
			
			if (skill.isDestinationReached())
			{
				triggerEvent(EEvent.PREPARED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET_BEFORE;
		}
	}
	
	private class AimState implements IRoleState
	{
		private AMoveToSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(target);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (skill.isDestinationReached())
			{
				triggerEvent(EEvent.AIMED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.AIM;
		}
	}
	
	
	private class PushState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			// FIXME
			// setNewSkill(new MoveBallToSkill(target));
		}
		
		
		@Override
		public void doUpdate()
		{
			WorldFrame wFrame = getAiFrame().getWorldFrame();
			if (wFrame.getBall().getPos().equals(target, BALL_REACHED_TARGET_TOL))
			{
				triggerEvent(EEvent.BALL_POSITIONED);
			} else
			{
				triggerEvent(EEvent.BALL_LOST);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PUSH;
		}
	}
}
