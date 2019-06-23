/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickTestSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


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
	
	private final Random				rnd				= new Random(System.currentTimeMillis());
	
	private final int					durLow;
	private final int					durHigh;
	private final int					dribbleLow;
	private final int					dribbleHigh;
	
	private final IVector2			ballPos;
	private static final double	INIT_ANGLE		= 0;
	
	@Configurable(comment = "Time [ms] to wait before doing the chipkick")
	private static long				waitingTime		= 1500;
	
	@Configurable(comment = "Dist [mm] to ball in prepare state")
	private static double			dist2Ball		= Geometry.getBotRadius()
			+ Geometry.getBallRadius()
			+ 10;
	
	@Configurable(comment = "Dist [mm] to ball when it is considered to be at its destination")
	private static double			ballAtDestTol	= 50;
	
	
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
		ballPos = new Vector2(Geometry.getGoalOur().getGoalCenter()).add(new Vector2(0, Geometry.getFieldWidth() / 4.0));
		
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
			double duration = rnd.nextInt((durHigh - durLow) + 1) + durLow;
			int dribble = rnd.nextInt((dribbleHigh - dribbleLow) + 1) + dribbleLow;
			KickTestSkill skill = new KickTestSkill(new DynamicPosition(AVector2.ZERO_VECTOR), duration);
			skill.setDribbleSpeed(dribble);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (((getWFrame().getTimestamp() - getBot().getBot().getLastKickTime()) / 1e9) < 0.5)
			{
				triggerEvent(EEvent.CHIPPED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DO;
		}
	}
	
	private class PrepareState implements IRoleState
	{
		private AMoveToSkill skill = null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
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
			if (getWFrame().getBall().getPos().equals(ballPos, ballAtDestTol))
			{
				triggerEvent(EEvent.PREPARED);
			}
		}
		
		
		@Override
		public void doExitActions()
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
		private long startTime = 0;
		
		
		@Override
		public void doEntryActions()
		{
			startTime = getWFrame().getTimestamp();
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWFrame().getTimestamp() - startTime) > TimeUnit.MILLISECONDS.toNanos(waitingTime))
			{
				triggerEvent(EEvent.DONE);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.WAIT;
		}
	}
}
