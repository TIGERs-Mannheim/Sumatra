/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.11.2012
 * Author(s): jan, SebastianN
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * 
 * This role will generate points in the enemys goal and evaluate them.
 * The target for the shoot will be an unblocked point.
 * 
 * @author janE
 * 
 */
public class ShooterV2Role extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private IVector2	bestTarget	= null;
	private boolean	enableChip	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public ShooterV2Role()
	{
		super(ERole.SHOOTERV2);
		IRoleState shootState = new ShootState();
		setInitialState(shootState);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.KICKED);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.TIMED_OUT);
		addEndTransition(EStateId.SHOOT, EEvent.NO_TARGET);
	}
	
	
	/**
	 * @param needsReadyToShoot
	 */
	public ShooterV2Role(boolean needsReadyToShoot)
	{
		super(ERole.SHOOTERV2);
		IRoleState shootState = new ShootState();
		IRoleState prepareState = new PrepareState();
		if (needsReadyToShoot)
		{
			setInitialState(prepareState);
		} else
		{
			setInitialState(shootState);
		}
		addTransition(EStateId.PREPARE, EEvent.READY, shootState);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.KICKED);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.TIMED_OUT);
		addEndTransition(EStateId.SHOOT, EEvent.NO_TARGET);
	}
	
	
	/**
	 * @param needsReadyToShoot
	 * @param enableChip true for allow to use the Chipkicker
	 */
	public ShooterV2Role(boolean needsReadyToShoot, boolean enableChip)
	{
		super(ERole.SHOOTERV2);
		this.enableChip = enableChip;
		IRoleState shootState = new ShootState();
		IRoleState prepareState = new PrepareState();
		if (needsReadyToShoot)
		{
			setInitialState(prepareState);
		} else
		{
			setInitialState(shootState);
		}
		addTransition(EStateId.PREPARE, EEvent.READY, shootState);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.KICKED);
		addEndTransition(EStateId.SHOOT, KickAutoSkill.EEvent.TIMED_OUT);
		addEndTransition(EStateId.SHOOT, EEvent.NO_TARGET);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		SHOOT,
	}
	
	private enum EEvent
	{
		NO_TARGET,
		
		/**
		 * READY in case the bot is supposed to shot immediately.
		 */
		READY
	}
	
	
	private class PrepareState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
			if (bestTarget == null)
			{
				bestTarget = getAiFrame().tacticalInfo.getBestDirectShootTarget();
			}
			if (bestTarget != null)
			{
				updateDestination(GeoMath.stepAlongLine(getPos(), bestTarget, -AIConfig.getGeneral(getBotType())
						.getPositioningPreAiming() / 2));
				updateLookAtTarget(bestTarget);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			if (bestTarget == null)
			{
				bestTarget = getAiFrame().tacticalInfo.getBestDirectShootTarget();
				updateDestination(GeoMath.stepAlongLine(getPos(), bestTarget, -AIConfig.getGeneral(getBotType())
						.getPositioningPreAiming() / 2));
				updateLookAtTarget(bestTarget);
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
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class ShootState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			bestTarget = getAiFrame().tacticalInfo.getBestDirectShootTarget();
			
			// In case the shot was supposed to be shot immediately and no target was found
			if (bestTarget == null)
			{
				nextState(EEvent.NO_TARGET);
				return;
			}
			if (enableChip && (bestTarget != null))
			{
				// check if target is block or not use chip if blocked
				float tripleBallRadius = 3 * AIConfig.getGeometry().getBallRadius();
				boolean chipOrStreight = GeoMath.p2pVisibility(getAiFrame().worldFrame, getPos(), bestTarget,
						tripleBallRadius, new ArrayList<BotID>());
				if (chipOrStreight)
				{
					setNewSkill(new KickAutoSkill(bestTarget, TigerDevices.KICKER_MAX));
				} else
				{
					setNewSkill(new ChipAutoSkill(bestTarget, 1.0f));
				}
			} else
			{
				setNewSkill(new KickAutoSkill(bestTarget, TigerDevices.KICKER_MAX));
				
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
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param readyVal
	 */
	public void setReady(boolean readyVal)
	{
		nextState(EEvent.READY);
	}
	
	
	/**
	 * @return
	 */
	public boolean isShooting()
	{
		return (getCurrentState() == EStateId.SHOOT);
	}
	
	
	@Override
	public void fillNeededFeatures(List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getTarget()
	{
		return bestTarget;
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame aiFrame)
	{
		if (bestTarget != null)
		{
			getAiFrame().addDebugShape(
					new DrawableLine(new Line(getAiFrame().worldFrame.ball.getPos(), bestTarget
							.subtractNew(getAiFrame().worldFrame.ball.getPos())), Color.blue));
			
			IVector2 start = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), bestTarget, -500);
			getAiFrame().addDebugShape(new DrawableLine(Line.newLine(start, bestTarget), Color.blue));
		}
	}
	
}
