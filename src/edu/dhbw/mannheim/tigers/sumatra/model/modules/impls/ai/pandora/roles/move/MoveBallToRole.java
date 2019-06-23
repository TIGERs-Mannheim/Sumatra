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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.GetBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveBallToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallSplineSkill;
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
	public MoveBallToRole()
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
		GetState getState = new GetState();
		PushState pushState = new PushState();
		AimState aimState = new AimState();
		addTransition(EStateId.GET_BEFORE, EEvent.PREPARED, aimState);
		addTransition(EStateId.AIM, EEvent.AIMED, getState);
		addTransition(EStateId.GET, EEvent.BALL_GOT, pushState);
		addTransition(EStateId.PUSH, EEvent.BALL_LOST, getBeforeState);
		addEndTransition(EStateId.GET, EEvent.BALL_POSITIONED);
		addEndTransition(EStateId.PUSH, EEvent.BALL_POSITIONED);
		setInitialState(getBeforeState);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
			skill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			skill.getMoveCon().updateDestination(
					GeoMath.stepAlongLine(getAiFrame().getWorldFrame().ball.getPos(), target, -positioningPreAiming - 100));
			skill.getMoveCon().updateLookAtTarget(target);
			skill.getMoveCon().setPenaltyAreaAllowed(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().updateDestination(
					GeoMath.stepAlongLine(getAiFrame().getWorldFrame().ball.getPos(), target, -positioningPreAiming - 100));
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
			nextState(EEvent.PREPARED);
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
			setNewSkill(new TurnAroundBallSplineSkill(target));
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
			nextState(EEvent.AIMED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.AIM;
		}
	}
	
	
	private class GetState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new GetBallSkill(true));
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
			nextState(EEvent.BALL_GOT);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET;
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
			if (wFrame.ball.getPos().equals(target, BALL_REACHED_TARGET_TOL))
			{
				nextState(EEvent.BALL_POSITIONED);
			} else
			{
				nextState(EEvent.BALL_LOST);
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
					new DrawableLine(new Line(getWFrame().ball.getPos(), target.subtractNew(getWFrame().ball.getPos())),
							Color.blue, true));
			getAiFrame().addDebugShape(new DrawablePoint(target, Color.yellow));
		}
	}
}
