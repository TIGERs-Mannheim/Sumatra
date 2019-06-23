/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelocity;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;

import java.util.ArrayList;
import java.util.List;

import static edu.tigers.sumatra.math.SumatraMath.isZero;


/**
 * Move to a given destination and orientation with PositionController
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class PullBackSkillV2 extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param target target
	 */
	public PullBackSkillV2(final IVector2 target)
	{
		super(ESkill.PULL_BACKV2);
		IState pushBallState = new PushBallState();
		setInitialState(pushBallState);
		addTransition(EEvent.PUSH_BALL, new PushBallState());
		addTransition(EEvent.IDLE, IDLE_STATE);
	}
	
	
	private enum EEvent implements IEvent
	{
		PUSH_BALL,
		PULL_BALL,
		IDLE
	}
	
	
	private class PushBallState implements IState
	{
		private IVector2		initDir			= null;					// botToBall
		private long			timer				= 0;
		private long			losttimer		= 0;
		private List<Long>	waitTimes		= new ArrayList<>();
		private int				idx				= 0;
		private long			contactTimer	= 0;
		private double			maxSpeed			= 0.15;
		
		
		@Override
		public void doEntryActions()
		{
			initDir = getWorldFrame().getBall().getPos().subtractNew(getPos()).normalizeNew();
			timer = getWorldFrame().getTimestamp();
			contactTimer = 0;
			maxSpeed = 0.15;
			
			
			idx = 0;
			waitTimes.clear();
			waitTimes.add(500_000_000L); // push ball forward, start slow get faster
			waitTimes.add(500_000_000L); // push ball start fast, get slower
			waitTimes.add(1_000_000_000L); // pull ball start slow, get fast
		}
		
		
		@Override
		public void doUpdate()
		{
			double waitTime = waitTimes.get(idx);
			double progress = Math.min(1, (getWorldFrame().getTimestamp() - timer) / waitTime);
			double contactTime = (getWorldFrame().getTimestamp() - contactTimer) * 1e-9;
			if (isZero(contactTime))
			{
				contactTime = 0;
			}
			double speed = 0;
			if (idx == 0)
			{
				// push ball forward start slow, end fast
				speed = progress * 0.15;
			} else if (idx == 1)
			{
				speed = (1 - progress) * maxSpeed;
			} else if (idx == 2)
			{
				speed = -progress * 0.15;
			}
			
			if (progress >= 1)
			{
				if ((idx < (waitTimes.size() - 1)) && getTBot().hasBallContact())
				{
					idx++;
					timer = getWorldFrame().getTimestamp();
				} else if ((idx == 0) && getTBot().hasBallContact() && (contactTime > 0.1))
				{
					idx++;
					timer = getWorldFrame().getTimestamp();
					maxSpeed = speed;
				}
			}
			
			if (getTBot().hasBallContact())
			{
				if (contactTimer == 0)
				{
					contactTimer = getWorldFrame().getTimestamp();
				}
			} else
			{
				contactTimer = 0;
			}
			IVector2 vel = initDir.scaleToNew(speed);
			BotSkillGlobalVelocity skill = setGlobalVelocity(vel, 0, getMoveCon().getMoveConstraints());
			double dribble = ((idx + Math.max(1, progress)) * 5000) + 1000;
			skill.getKickerDribbler().setDribblerSpeed(dribble);
			
			if (!getTBot().hasBallContact() && (idx >= 2))
			{
				if (losttimer == 0)
				{
					losttimer = getWorldFrame().getTimestamp();
				} else if ((getWorldFrame().getTimestamp() - losttimer) > 0.5e9)
				{
					triggerEvent(EEvent.IDLE);
				}
			} else
			{
				losttimer = 0;
			}
		}
		
		
	}
	
	
	/**
	 * @return true, if in idle state
	 */
	public boolean isIdle()
	{
		return getCurrentState() == IDLE_STATE;
	}
	
}
