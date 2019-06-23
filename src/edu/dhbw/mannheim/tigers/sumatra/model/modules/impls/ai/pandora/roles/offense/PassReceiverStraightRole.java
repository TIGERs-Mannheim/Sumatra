/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ReceiveBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * This role will receive a pass from a passer.
 * It will look at the passer and take the ball on its dribbler.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PassReceiverStraightRole extends AReceiverRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final float	BALL_SPEED_VISUALIZE_FACTOR	= 100;
	private boolean				passerShot							= false;
	private IVector2				initPosition;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * This role will receiver a pass from a sender, looking to to ball
	 */
	public PassReceiverStraightRole()
	{
		super(ERole.PASS_RECEIVER_STRAIGHT);
		setInitialState(new PrepareState());
		addTransition(EStateId.PREPARE, EEvent.PASSER_SHOT, new ReceiveState());
		addEndTransition(EStateId.RECEIVE, EEvent.RECEIVED);
		addEndTransition(EStateId.RECEIVE, EEvent.LOST);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		RECEIVE
	}
	
	private enum EEvent
	{
		PASSER_SHOT,
		RECEIVED,
		LOST
	}
	
	private class PrepareState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			initPosition = getPos();
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			updateDestination(initPosition);
			updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
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
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class ReceiveState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new ReceiveBallSkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			final TrackedBall ball = getAiFrame().worldFrame.ball;
			if (!ball.getVel().isZeroVector())
			{
				final ILine line = new Line(ball.getPos(), ball.getVel().multiplyNew(BALL_SPEED_VISUALIZE_FACTOR));
				getAiFrame().addDebugShape(new DrawableLine(line, Color.orange));
				final IVector2 leadPoint = GeoMath.leadPointOnLine(getPos(), line);
				final float ballSpeedCorrection = 0;
				final IVector2 dest = GeoMath.stepAlongLine(leadPoint, ball.getPos(), -ballSpeedCorrection);
				getAiFrame().addDebugShape(new DrawablePoint(dest));
				updateDestination(dest);
			} else
			{
				nextState(EEvent.LOST);
			}
			updateLookAtTarget(ball.getPos());
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
			nextState(EEvent.RECEIVED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.RECEIVE;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean isReady()
	{
		return checkMoveCondition(getAiFrame().worldFrame);
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.DRIBBLER);
		features.add(EFeature.MOVE);
	}
	
	
	@Override
	public void setReady()
	{
		if (!passerShot)
		{
			passerShot = true;
			nextState(EEvent.PASSER_SHOT);
		}
	}
	
	
	@Override
	public void setPassUsesChipper(boolean passUsesChipper)
	{
		
	}
	
	
	@Override
	public final void setInitPosition(IVector2 pos)
	{
		initPosition = pos;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
