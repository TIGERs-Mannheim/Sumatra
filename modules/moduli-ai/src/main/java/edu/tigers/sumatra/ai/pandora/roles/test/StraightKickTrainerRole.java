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
public class StraightKickTrainerRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Random				rnd					= new Random(System.currentTimeMillis());
	
	private final DynamicPosition	target;
	
	@Configurable(comment = "Time [ms] to wait before doing the kick")
	private static long				waitingTime			= 1000;
	
	@Configurable(comment = "Dist [mm] to ball in prepare state")
	private static double			dist2Ball			= Geometry.getBotRadius()
			+ Geometry.getBallRadius()
			+ 10;
	
	@Configurable(comment = "Dist [mm] to ball when it is considered to be at its destination")
	private static double			ballAtDestTol		= 50;
	
	@Configurable(comment = "Velocity tolerance [m/s] when the ball is considered to be moving")
	private static double			ballMovingVelTol	= 0.1;
	
	
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
			// double dist = GeoMath.distancePP(getWFrame().getBall().getPos(), target);
			double factor = 1; // Math.min(dist / 6000, 1);
			double kickSpeed = (factor * (rnd.nextInt((maxDur - minDur) + 1) + minDur)) / 1000.0;
			KickTestSkill skill = new KickTestSkill(target, kickSpeed);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWFrame().getBall().getVel().getLength() > 0.2)
			// Math.abs((getWFrame().getTimestamp() - getBot().getBot().getLastKickTime()) / 1e9) < 0.1)
			{
				triggerEvent(EEvent.KICKED);
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
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWFrame().getBall().getVel().getLength2() < ballMovingVelTol)
					&& Geometry.getField().isPointInShape(getWFrame().getBall().getPos()))
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
