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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
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
	
	private final Random		rnd					= new Random(System.currentTimeMillis());
	
	private final IVector2	target;
	
	@Configurable(comment = "Time [ms] to wait before doing the kick")
	private static long		waitingTime			= 1000;
	
	@Configurable(comment = "Dist [mm] to ball in prepare state")
	private static float		dist2Ball			= AIConfig.getGeometry().getBotRadius()
																	+ AIConfig.getGeometry().getBallRadius()
																	+ 10;
	
	@Configurable(comment = "Dist [mm] to ball when it is considered to be at its destination")
	private static float		ballAtDestTol		= 50;
	
	@Configurable(comment = "Velocity tolerance [m/s] when the ball is considered to be moving")
	private static float		ballMovingVelTol	= 0.1f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public StraightKickTrainerRole()
	{
		super(ERole.STRAIGHT_KICK_TRAINER);
		
		target = AIConfig.getGeometry().getGoalTheir()
				.getGoalCenter();
		
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
			int duration = (int) (factor * (rnd.nextInt(8000) + 2000));
			KickTestSkill skill = new KickTestSkill(target, duration);
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
			nextState(EEvent.KICKED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DO;
		}
	}
	
	private class PrepareState implements IRoleState
	{
		private MoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new MoveAndStaySkill();
			skill.getMoveCon().setPenaltyAreaAllowed(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWFrame().getBall().getVel().getLength2() < ballMovingVelTol)
					&& AIConfig.getGeometry().getField().isPointInShape(getWFrame().getBall().getPos()))
			{
				IVector2 dest = GeoMath.stepAlongLine(getWFrame().getBall().getPos(), target, -dist2Ball);
				skill.getMoveCon().updateDestination(dest);
				skill.getMoveCon().updateLookAtTarget(target);
				
				if (skill.getMoveCon().checkCondition(getWFrame(), getBotID()) == EConditionState.FULFILLED)
				{
					nextState(EEvent.PREPARED);
				}
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
			startTime = System.nanoTime();
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(waitingTime))
			{
				nextState(EEvent.DONE);
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
