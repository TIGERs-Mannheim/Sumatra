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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Train chip kicker
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipKickTrainerV2Role extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Random			rnd				= new Random(System.currentTimeMillis());
	
	private final int				durLow;
	private final int				durHigh;
	private final int				dribbleLow;
	private final int				dribbleHigh;
	
	private final IVector2		ballPos;
	private static final float	INIT_ANGLE		= 0;
	
	@Configurable(comment = "Time [ms] to wait before doing the chipkick")
	private static long			waitingTime		= 1500;
	
	@Configurable(comment = "Dist [mm] to ball in prepare state")
	private static float			dist2Ball		= AIConfig.getGeometry().getBotRadius()
																	+ AIConfig.getGeometry().getBallRadius()
																	+ 10;
	
	@Configurable(comment = "Dist [mm] to ball when it is considered to be at its destination")
	private static float			ballAtDestTol	= 50;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param durLow
	 * @param durHigh
	 * @param dribbleLow
	 * @param dribbleHigh
	 */
	public ChipKickTrainerV2Role(final int durLow, final int durHigh, final int dribbleLow,
			final int dribbleHigh)
	{
		super(ERole.CHIP_KICK_TRAINER_V2);
		this.durLow = durLow;
		this.durHigh = durHigh;
		this.dribbleLow = dribbleLow;
		this.dribbleHigh = dribbleHigh;
		ballPos = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter()).add(new Vector2(0, AIConfig
				.getGeometry().getFieldWidth() / 4));
		
		IRoleState doState = new DoState();
		IRoleState prepareState = new PrepareState();
		IRoleState waitState = new WaitState();
		setInitialState(prepareState);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, waitState);
		addTransition(EStateId.DO, EEvent.CHIPPED, prepareState);
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
		CHIPPED
	}
	
	private class DoState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			int duration = rnd.nextInt((durHigh - durLow) + 1) + durLow;
			int dribble = rnd.nextInt((dribbleHigh - dribbleLow) + 1) + dribbleLow;
			ChipTestSkill skill = new ChipTestSkill(duration, dribble);
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
			nextState(EEvent.CHIPPED);
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
			Vector2 dest = new Vector2(ballPos);
			if (dest.x() > 0)
			{
				dest.setX(dest.x() + dist2Ball);
			} else
			{
				dest.setX(dest.x() - dist2Ball);
			}
			
			skill.getMoveCon().updateDestination(dest);
			skill.getMoveCon().updateTargetAngle(INIT_ANGLE);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWFrame().getBall().getPos().similar(ballPos, ballAtDestTol))
			{
				nextState(EEvent.PREPARED);
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
