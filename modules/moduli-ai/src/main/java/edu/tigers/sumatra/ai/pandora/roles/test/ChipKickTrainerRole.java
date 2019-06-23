/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickTestSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.VisionWatcher;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Train chip kicker
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipKickTrainerRole extends ARole
{
	private static final Logger	log	= Logger.getLogger(ChipKickTrainerRole.class.getName());
	private final Random				rnd	= new Random(System.currentTimeMillis());
	
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
			int kickSpeed = rnd.nextInt((durHigh - durLow) + 1) + durLow;
			int dribble = rnd.nextInt((dribbleHigh - dribbleLow) + 1) + dribbleLow;
			log.debug(durLow + " " + durHigh + " " + dribbleLow + " " + dribbleHigh);
			log.debug("kickSpeed=" + kickSpeed + " dribble=" + dribble);
			KickTestSkill skill = new KickTestSkill(new DynamicPosition(AVector2.ZERO_VECTOR), kickSpeed);
			skill.setDribbleSpeed(dribble);
			setNewSkill(skill);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String fileName = "chipKick/" + sdf.format(new Date());
			VisionWatcher ballWatcher = new VisionWatcher(fileName);
			ballWatcher.setStopAutomatically(true);
			ballWatcher.start();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (((getWFrame().getTimestamp() - getBot().getBot().getLastKickTime()) / 1e9) < 0.1)
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
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			double dist2Ball = 180;
			IVector2 ballTarget = getBallTarget();
			IVector2 dest = GeoMath.stepAlongLine(getWFrame().getBall().getPos(), ballTarget, -dist2Ball);
			skill.getMoveCon().updateDestination(dest);
			skill.getMoveCon().updateLookAtTarget(ballTarget);
			
			double dist = GeoMath.distancePP(dest, getPos());
			if (dist < 50)
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
			if ((getWFrame().getTimestamp() - startTime) > TimeUnit.MILLISECONDS.toNanos(2000))
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
	
	
	private IVector2 getBallTarget()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		double x = -Math.signum(ballPos.x()) * Geometry.getFieldLength();
		double y = -Math.signum(ballPos.y()) * Geometry.getFieldWidth();
		return new Vector2(x, y);
	}
}
