/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * This Role will pass the ball to a corresponding PassReceiver
 * 
 * @author GuntherB
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PassSenderRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log				= Logger.getLogger(PassSenderRole.class.getName());
	private IVector2					receiverTarget	= new Vector2f();
	private boolean					receiverReady	= false;
	private final float				shootEndVel;
	private boolean					enableChip		= false;
	
	private Vector2					destination;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Default constructor
	 */
	public PassSenderRole()
	{
		this(Vector2.ZERO_VECTOR, 0);
		setReceiverReady();
	}
	
	
	/**
	 * Constructor witch will enable the Chip-Kicker
	 * @param receiverTarget
	 * @param enableChip
	 */
	public PassSenderRole(IVector2 receiverTarget, boolean enableChip)
	{
		super(ERole.PASS_SENDER);
		setInitialState(new WaitingState());
		addTransition(EStateId.WAIT, EEvent.RECEIVER_READY, new PrepareState());
		addTransition(EStateId.PREPARE, EEvent.PREPARED, new ShootState());
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.KICKED);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.TIMED_OUT);
		addEndTransition(EStateId.SHOOT, EEvent.TIMEOUT);
		this.receiverTarget = receiverTarget;
		shootEndVel = AIConfig.getRoles().getPassSenderBallEndVel();
		this.enableChip = enableChip;
	}
	
	
	/**
	 * Constructor witch will enable the Chip-Kicker
	 * @param receiverTarget
	 * @param enableChip
	 * @param shootEndVel
	 */
	public PassSenderRole(IVector2 receiverTarget, boolean enableChip, float shootEndVel)
	{
		super(ERole.PASS_SENDER);
		setInitialState(new WaitingState());
		addTransition(EStateId.WAIT, EEvent.RECEIVER_READY, new PrepareState());
		addTransition(EStateId.PREPARE, EEvent.PREPARED, new ShootState());
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.KICKED);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.TIMED_OUT);
		this.receiverTarget = receiverTarget;
		this.shootEndVel = shootEndVel;
		this.enableChip = enableChip;
	}
	
	
	/**
	 * creates a new Role, that will pass the ball to a target destination
	 * @param receiverTarget
	 * @see PassSenderRole
	 */
	public PassSenderRole(IVector2 receiverTarget)
	{
		this(receiverTarget, AIConfig.getRoles().getPassSenderBallEndVel());
	}
	
	
	/**
	 * creates a new Role, that will pass the ball to a target destination
	 * @param receiverTarget
	 * @param shootEndVel
	 * @see PassSenderRole
	 */
	public PassSenderRole(IVector2 receiverTarget, float shootEndVel)
	{
		super(ERole.PASS_SENDER);
		setInitialState(new WaitingState());
		addTransition(EStateId.WAIT, EEvent.RECEIVER_READY, new PrepareState());
		addTransition(EStateId.PREPARE, EEvent.PREPARED, new ShootState());
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.KICKED);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.TIMED_OUT);
		this.receiverTarget = receiverTarget;
		this.shootEndVel = shootEndVel;
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		WAIT,
		PREPARE,
		SHOOT
	}
	
	private enum EEvent
	{
		RECEIVER_READY,
		TIMEOUT,
		PREPARED
	}
	
	private class WaitingState implements IRoleState
	{
		private float	positioningPreAiming;
		
		
		@Override
		public void doEntryActions()
		{
			positioningPreAiming = AIConfig.getGeneral(getBotType()).getPositioningPreAiming();
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			destination = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), receiverTarget,
					-positioningPreAiming);
			// if destination is in penealtyArea
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(destination))
			{
				destination = new Vector2(AIConfig.getGeometry().getPenaltyAreaOur().nearestPointOutside(destination));
			}
			
			updateDestination(destination);
			updateLookAtTarget(receiverTarget);
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
			return EStateId.WAIT;
		}
	}
	
	private class PrepareState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			float positioningPreAiming = AIConfig.getGeneral(getBotType()).getPositioningPreAiming();
			IVector2 dest = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), receiverTarget, -((AIConfig
					.getGeometry().getBotRadius() + positioningPreAiming)));
			updateDestination(dest);
			updateLookAtTarget(receiverTarget);
			
			if (dest.equals(getPos(), positioningPreAiming)
					&& (GeoMath.distancePP(getPos(), dest) < positioningPreAiming)
					&& (AngleMath.getShortestRotation(getMoveCon().getAngleCon().getTargetAngle(), getBot().getAngle()) < AngleMath.PI_QUART))
			{
				nextState(EEvent.PREPARED);
			} else
			{
				setNewSkill(new MoveToSkill(getMoveCon()));
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getMoveCon().getDestFreeCon().checkCondition(getAiFrame().worldFrame, getBotID()) == EConditionState.BLOCKED)
					&& (getMoveCon().getDestCon().checkCondition(getAiFrame().worldFrame, getBotID()) != EConditionState.PENDING)
					&& (getMoveCon().getAngleCon().checkCondition(getAiFrame().worldFrame, getBotID()) != EConditionState.PENDING))
			{
				nextState(EEvent.PREPARED);
			}
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
			return EStateId.PREPARE;
		}
	}
	
	private class ShootState implements IRoleState
	{
		private int	retries	= 3;
		
		
		@Override
		public void doEntryActions()
		{
			log.debug("Shooting state");
			final float length = GeoMath.distancePP(getPos(), receiverTarget);
			// Add chip kicker
			
			if (enableChip)
			{
				float tripleBallRadius = AIConfig.getGeometry().getBallRadius();
				List<BotID> ignoreBots = new ArrayList<BotID>(2);
				ignoreBots.add(getBotID());
				IVector2 rec = GeoMath.stepAlongLine(receiverTarget, getPos(), AIConfig.getGeometry().getBotRadius() * 2);
				
				boolean kick = GeoMath.p2pVisibility(getAiFrame().worldFrame, getPos(), rec, tripleBallRadius, getBotID());
				
				if (kick)
				{
					setNewSkill(new KickAutoSkill(receiverTarget, length, shootEndVel));
				} else
				{
					setNewSkill(new ChipAutoSkill(receiverTarget, 1.0f));
				}
			} else
			{
				setNewSkill(new KickAutoSkill(receiverTarget, length, shootEndVel));
			}
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
			if (retries <= 0)
			{
				nextState(EEvent.TIMEOUT);
			}
			if (getAiFrame().worldFrame.ball.getVel().getLength2() < 0.1)
			{
				log.debug("retry");
				retries--;
				doEntryActions();
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.SHOOT;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void afterUpdate(AIInfoFrame aiFrame)
	{
		getAiFrame().addDebugShape(
				new DrawableLine(new Line(getPos(), receiverTarget.subtractNew(getPos())), Color.blue, true));
		getAiFrame().addDebugShape(
				new DrawableLine(new Line(getPos(), new Vector2(getBot().getAngle()).scaleTo(AIConfig.getGeometry()
						.getFieldLength())), Color.red, false));
	}
	
	
	/**
	 * update the position of the receiver; the passer will aim to it
	 * 
	 * @param newReceiverPos
	 */
	public void updateReceiverPos(IVector2 newReceiverPos)
	{
		receiverTarget = newReceiverPos;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public final void setReceiverReady()
	{
		if (!receiverReady)
		{
			receiverReady = true;
			nextState(EEvent.RECEIVER_READY);
		}
	}
	
	
	/**
	 * Use {@link #setReceiverReady()} instead
	 * 
	 * @param ready this param is deprecated
	 */
	public void setReceiverReady(boolean ready)
	{
		setReceiverReady();
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		if (enableChip)
		{
			features.add(EFeature.CHIP_KICKER);
		}
	}
}
