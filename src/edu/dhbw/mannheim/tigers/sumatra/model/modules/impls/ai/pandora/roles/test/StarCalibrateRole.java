/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IdleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.StraightMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This calibrate role will let the bot drive a star.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class StarCalibrateRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(StarCalibrateRole.class.getName());
	private static final int		SIZE	= 1000;
	private final List<Float>		diffs	= new ArrayList<Float>();
	private final List<Long>		times	= new ArrayList<Long>();
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EState
	{
		N(SIZE, 0),
		NE(SIZE, (AngleMath.PI_HALF * 3) + AngleMath.PI_QUART),
		E(SIZE, AngleMath.PI_HALF * 3),
		SE(SIZE, AngleMath.PI + AngleMath.PI_QUART),
		S(SIZE, AngleMath.PI),
		SW(SIZE, AngleMath.PI_HALF + AngleMath.PI_QUART),
		W(SIZE, AngleMath.PI_HALF),
		NW(SIZE, AngleMath.PI_QUART), ;
		
		public final int		straight;
		public final float	angle;
		
		
		private EState(int straight, float angle)
		{
			this.straight = straight;
			this.angle = angle;
		}
	}
	
	private enum EEvent
	{
		DEFAULT_DONE,
		REVERSE_DONE, ;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public StarCalibrateRole()
	{
		super(ERole.STAR_CALIBRATE);
		
		List<EState> states = Arrays.asList(EState.values());
		
		IRoleState initState = new DefaultState(states.get(0));
		setInitialState(initState);
		addTransition(states.get(0), EEvent.DEFAULT_DONE, new ReverseState(states.get(0)));
		
		for (int i = 1; i < states.size(); i++)
		{
			EState lastState = states.get(i - 1);
			EState nextState = states.get(i);
			addTransition(lastState, EEvent.REVERSE_DONE, new DefaultState(nextState));
			addTransition(nextState, EEvent.DEFAULT_DONE, new ReverseState(nextState));
		}
		
		addTransition(states.get(states.size() - 1), EEvent.REVERSE_DONE, initState);
	}
	
	private abstract class BaseState implements IRoleState
	{
		protected final EState	state;
		private IVector2			startPos		= null;
		private long				startTime	= 0;
		
		protected int				straight;
		protected float			angle;
		
		
		public BaseState(EState state)
		{
			this.state = state;
			straight = state.straight;
			angle = state.angle;
		}
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new StraightMoveSkill(straight, angle));
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
			IVector2 endPos = getPos();
			IVector2 botOrientation = new Vector2(getBot().getAngle()).scaleTo(straight);
			IVector2 desiredPos = startPos.addNew(botOrientation.turnNew(angle));
			float diff = desiredPos.subtractNew(endPos).getLength2();
			diffs.add(diff);
			
			long timeDiff = SumatraClock.nanoTime() - startTime;
			times.add(TimeUnit.NANOSECONDS.toMillis(timeDiff));
			
			log.info("Diffs: " + diffs);
			log.info("Times: " + times);
		}
		
		
		@Override
		public void onSkillStarted(ISkill skill, BotID botID)
		{
			if (skill.getSkillName() != ESkillName.IDLE)
			{
				startPos = getPos();
				startTime = SumatraClock.nanoTime();
			}
		}
		
		
		@Override
		public final void onSkillCompleted(ISkill skill, BotID botID)
		{
			if (skill.getSkillName() == ESkillName.IDLE)
			{
				onStateFinished();
			} else
			{
				setNewSkill(new IdleSkill());
			}
		}
		
		
		protected abstract void onStateFinished();
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return state;
		}
	}
	
	/**
	 * DefaultState
	 */
	private class DefaultState extends BaseState
	{
		public DefaultState(EState state)
		{
			super(state);
		}
		
		
		@Override
		protected void onStateFinished()
		{
			triggerEvent(EEvent.DEFAULT_DONE);
		}
	}
	
	/**
	 * ReverseState
	 */
	private class ReverseState extends BaseState
	{
		public ReverseState(EState state)
		{
			super(state);
			angle = AngleMath.normalizeAngle(state.angle + AngleMath.PI);
		}
		
		
		@Override
		protected void onStateFinished()
		{
			triggerEvent(EEvent.REVERSE_DONE);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(List<EFeature> features)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
