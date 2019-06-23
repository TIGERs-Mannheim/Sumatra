/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 14, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.GetBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveBallToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


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
	private static final float	BALL_REACHED_TARGET_TOL	= 80;
	
	private final IVector2		target;
	
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
		this(Vector2.ZERO_VECTOR);
	}
	
	
	/**
	 * @param target
	 */
	public MoveBallToRole(IVector2 target)
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
		
		@Override
		public void doEntryActions()
		{
			updateDestination(GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), target,
					-AIConfig.getGeneral(getBotType()).getPositioningPreAiming() - 100));
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
			return EStateId.GET_BEFORE;
		}
	}
	
	private class AimState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new TurnAroundBallSkill(target));
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
			if (getMoveCon().getAngleCon().checkCondition(getAiFrame().worldFrame, getBotID()) == EConditionState.FULFILLED)
			{
				nextState(EEvent.AIMED);
			} else
			{
				doEntryActions();
			}
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
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
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
			WorldFrame wFrame = getAiFrame().worldFrame;
			if (wFrame == null)
			{
				return;
			}
			if (wFrame.ball.getPos().equals(target, BALL_REACHED_TARGET_TOL))
			{
				nextState(EEvent.BALL_POSITIONED);
			} else
			{
				nextState(EEvent.BALL_LOST);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PUSH;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		updateDestination(GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), target,
				-AIConfig.getGeneral(getBotType()).getPositioningPreAiming() - 100));
		
		if (getCurrentState() != EStateId.GET_BEFORE)
		{
			currentFrame.addDebugShape(new DrawableLine(new Line(currentFrame.worldFrame.ball.getPos(), target
					.subtractNew(currentFrame.worldFrame.ball.getPos())), Color.blue, true));
			currentFrame.addDebugShape(new DrawablePoint(target, Color.yellow));
		}
	}
}
