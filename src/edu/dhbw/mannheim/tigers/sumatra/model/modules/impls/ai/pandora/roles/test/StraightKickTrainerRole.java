/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Train chip kicker
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StraightKickTrainerRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Random				rnd					= new Random(SumatraClock.currentTimeMillis());
	
	private final DynamicPosition	target;
	
	@Configurable(comment = "Time [ms] to wait before doing the kick")
	private static long				waitingTime			= 1000;
	
	@Configurable(comment = "Dist [mm] to ball in prepare state")
	private static float				dist2Ball			= AIConfig.getGeometry().getBotRadius()
																			+ AIConfig.getGeometry().getBallRadius()
																			+ 10;
	
	@Configurable(comment = "Dist [mm] to ball when it is considered to be at its destination")
	private static float				ballAtDestTol		= 50;
	
	@Configurable(comment = "Velocity tolerance [m/s] when the ball is considered to be moving")
	private static float				ballMovingVelTol	= 0.1f;
	
	
	private final int					minDur, maxDur;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 * @param minDur
	 * @param maxDur
	 */
	public StraightKickTrainerRole(final DynamicPosition target, final int minDur, final int maxDur)
	{
		super(ERole.STRAIGHT_KICK_TRAINER);
		this.minDur = minDur;
		this.maxDur = maxDur;
		this.target = target;
		
		IRoleState doState = new DoState();
		IRoleState prepareState = new PrepareState();
		IRoleState waitState = new WaitState();
		setInitialState(prepareState);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, waitState);
		addTransition(EStateId.DO, EEvent.KICKED, prepareState);
		addTransition(EStateId.WAIT, EEvent.DONE, doState);
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		DO,
		PREPARE,
		WAIT
	}
	
	private enum EEvent
	{
		DONE,
		PREPARED,
		KICKED
	}
	
	private class DoState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			// float dist = GeoMath.distancePP(getWFrame().getBall().getPos(), target);
			float factor = 1; // Math.min(dist / 6000f, 1f);
			int duration = (int) (factor * (rnd.nextInt((maxDur - minDur) + 1) + minDur));
			KickTestSkill skill = new KickTestSkill(target, EKickMode.FIXED_DURATION, EMoveMode.CHILL, duration);
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
			triggerEvent(EEvent.KICKED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DO;
		}
	}
	
	private class PrepareState implements IRoleState
	{
		private IMoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWFrame().getBall().getVel().getLength2() < ballMovingVelTol)
					&& AIConfig.getGeometry().getField().isPointInShape(getWFrame().getBall().getPos()))
			{
				triggerEvent(EEvent.PREPARED);
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
			return EStateId.PREPARE;
		}
	}
	
	private class WaitState implements IRoleState
	{
		private long	startTime	= 0;
		
		
		@Override
		public void doEntryActions()
		{
			startTime = SumatraClock.nanoTime();
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((SumatraClock.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(waitingTime))
			{
				triggerEvent(EEvent.DONE);
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
			return EStateId.WAIT;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
