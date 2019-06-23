/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 14, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.MoveBallToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private static final float	BALL_REACHED_TARGET_TOL	= 100;
	
	private final IVector2		target;
	
	@Configurable(comment = "Dist [mm] - distance to ball before aiming")
	private static float			positioningPreAiming		= 150;
	
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
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.DRIBBLER);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class GetBeforeState implements IRoleState
	{
		private IMoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
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
			triggerEvent(EEvent.PREPARED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET_BEFORE;
		}
	}
	
	private class AimState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(target);
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
			triggerEvent(EEvent.AIMED);
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
			setNewSkill(new MoveBallToSkill(target));
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PUSH;
		}
	}
	
	
	@Override
	protected void afterUpdate()
	{
		if (getCurrentState() != EStateId.GET_BEFORE)
		{
			getAiFrame().addDebugShape(
					new DrawableLine(new Line(getWFrame().getBall().getPos(), target.subtractNew(getWFrame().getBall()
							.getPos())),
							Color.blue, true));
			getAiFrame().addDebugShape(new DrawablePoint(target, Color.yellow));
		}
	}
}
