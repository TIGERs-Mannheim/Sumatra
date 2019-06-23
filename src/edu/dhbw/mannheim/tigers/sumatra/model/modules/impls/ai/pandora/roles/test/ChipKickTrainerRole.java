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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Train chip kicker
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipKickTrainerRole extends ARole
{
	private static final Logger	log	= Logger.getLogger(ChipKickTrainerRole.class.getName());
	private final Random				rnd	= new Random(SumatraClock.currentTimeMillis());
	
	private final int					durLow;
	private final int					durHigh;
	private final int					dribbleLow;
	private final int					dribbleHigh;
	
	
	/**
	 * @param durLow
	 * @param durHigh
	 * @param dribbleLow
	 * @param dribbleHigh
	 */
	public ChipKickTrainerRole(final int durLow, final int durHigh, final int dribbleLow, final int dribbleHigh)
	{
		super(ERole.CHIP_KICK_TRAINER);
		this.durLow = durLow;
		this.durHigh = durHigh;
		this.dribbleLow = dribbleLow;
		this.dribbleHigh = dribbleHigh;
		
		IRoleState doState = new DoState();
		IRoleState prepareState = new PrepareState();
		IRoleState waitState = new WaitState();
		setInitialState(prepareState);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, doState);
		addTransition(EStateId.DO, EEvent.CHIPPED, waitState);
		addTransition(EStateId.WAIT, EEvent.DONE, prepareState);
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
			log.debug(durLow + " " + durHigh + " " + dribbleLow + " " + dribbleHigh);
			log.debug("Duration=" + duration + " dribble=" + dribble);
			ChipTestSkill skill = new ChipTestSkill(new DynamicPosition(AVector2.ZERO_VECTOR), duration, dribble);
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
			triggerEvent(EEvent.CHIPPED);
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
			skill.setDoComplete(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			float dist2Ball = 180;
			IVector2 ballTarget = getBallTarget();
			IVector2 dest = GeoMath.stepAlongLine(getWFrame().getBall().getPos(), ballTarget, -dist2Ball);
			skill.getMoveCon().updateDestination(dest);
			skill.getMoveCon().updateLookAtTarget(ballTarget);
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
			if ((SumatraClock.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(400))
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
	
	
	private IVector2 getBallTarget()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		float x = -Math.signum(ballPos.x()) * AIConfig.getGeometry().getFieldLength();
		float y = -Math.signum(ballPos.y()) * AIConfig.getGeometry().getFieldWidth();
		return new Vector2(x, y);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
