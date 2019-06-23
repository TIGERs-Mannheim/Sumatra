/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.DoNothingDriver;
import edu.tigers.sumatra.skillsystem.driver.KickBallTrajDriver;
import edu.tigers.sumatra.skillsystem.driver.PullBallDriver;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


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
	
	private IVector2					target			= null;
	
	
	private static final double	BALL_VEL_MAX	= 0.2;
	
	final int							minDribble		= 1000;
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
		this.target = target;
		setInitialState(new PushBallState());
		addTransition(EStateId.PUSH_BALL, new PushBallState());
		// addTransition(EStateId.GET_BALL, new GetBallState());
		addTransition(EStateId.IDLE, new IdleState());
	}
	
	
	private enum EStateId
	{
		GET_BALL,
		PUSH_BALL,
		PULL_BALL,
		IDLE;
	}
	
	private class IdleState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			DoNothingDriver driver = new DoNothingDriver();
			setPathDriver(driver);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			// if (getWorldFrame().getBall().getVel().getLength2() < BALL_VEL_MAX)
			// {
			// triggerEvent(EStateId.GET_BALL);
			// }
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.IDLE;
		}
		
	}
	
	@SuppressWarnings("unused")
	private class GetBallState implements IState
	{
		private KickBallTrajDriver	driver				= null;
		
		private long					ballConcatTimer	= 0;
		
		
		@Override
		public void doEntryActions()
		{
			IVector2 ballPos = getWorldFrame().getBall().getPos();
			double fieldWidth = Geometry.getFieldWidth();
			double fieldLength = Geometry.getFieldLength();
			
			IVector2 target2 = null;
			double dirX = 0;
			double dirY = 0;
			if (ballPos.x() > (fieldLength / 2.0))
			{
				dirX = 1;
			} else if (ballPos.x() < (-fieldLength / 2.0))
			{
				dirX = -1;
			}
			if (ballPos.y() > (fieldWidth / 2.0))
			{
				dirY = 1;
			} else if (ballPos.y() < (-fieldWidth / 2.0))
			{
				dirY = -1;
			}
			target2 = new Vector2(dirX, dirY);
			target2 = target2.normalizeNew();
			if ((dirX == 0) && (dirY == 0))
			{
				target2 = getWorldFrame().getBall().getPos()
						.addNew(target.subtractNew(getWorldFrame().getBall().getPos()).multiplyNew(-1).scaleToNew(5000));
			} else
			{
				target2 = ballPos.addNew(target2.multiplyNew(2000));
			}
			
			driver = new KickBallTrajDriver(new DynamicPosition(target2));
			driver.setDistBehindBallHitTarget(-10);
			setPathDriver(driver);
			getMoveCon().getMoveConstraints().setDefaultAccLimit();
			getMoveCon().getMoveConstraints().setDefaultVelLimit();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWorldFrame().getBall().getVel().getLength2() >= BALL_VEL_MAX)
					&& (GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos()) > 300))
			{
				triggerEvent(EStateId.IDLE);
				return;
			}
			
			double dist = GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos());
			if (dist < 600)
			{
				getMoveCon().getMoveConstraints().setVelMax(0.25);
				getMatchCtrl().setDribblerSpeed(minDribble * 2);
			} else if (dist < 1200)
			{
				getMoveCon().getMoveConstraints().setVelMax(0.5);
				getMatchCtrl().setDribblerSpeed(minDribble);
			} else
			{
				getMoveCon().getMoveConstraints().setDefaultAccLimit();
				getMoveCon().getMoveConstraints().setDefaultVelLimit();
			}
			if (getTBot().hasBallContact())
			{
				final double waitTime = 0.05; // 100ms
				if (ballConcatTimer == 0)
				{
					ballConcatTimer = getWorldFrame().getTimestamp();
				} else if ((getWorldFrame().getTimestamp() - ballConcatTimer) > (waitTime * 1e9)) // wait 100ms
				{
					triggerEvent(EStateId.PUSH_BALL);
				}
			} else
			{
				ballConcatTimer = 0;
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.GET_BALL;
		}
		
	}
	
	private class PushBallState implements IState
	{
		
		private PullBallDriver	driver			= new PullBallDriver();
		
		private IVector2			initDir			= null;						// botToBall
		
		private long				timer				= 0;
		
		private long				losttimer		= 0;
		
		private List<Long>		waitTimes		= new ArrayList<Long>();
		
		private int					idx				= 0;
		
		private long				contactTimer	= 0;
		double						maxSpeed			= 0.15;
		
		
		@Override
		public void doEntryActions()
		{
			initDir = getWorldFrame().getBall().getPos().subtractNew(getPos()).normalizeNew();
			driver = new PullBallDriver();
			timer = getWorldFrame().getTimestamp();
			contactTimer = 0;
			maxSpeed = 0.15;
			setPathDriver(driver);
			
			idx = 0;
			waitTimes.clear();
			waitTimes.add(0_500_000_000l); // push ball forward, start slow get faster
			waitTimes.add(0_500_000_000l); // push ball start fast, get slower
			waitTimes.add(1_000_000_000l); // pull ball start slow, get fast
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			double waitTime = waitTimes.get(idx);
			double progress = Math.min((getWorldFrame().getTimestamp() - timer), waitTime) / waitTime;
			double contactTime = (getWorldFrame().getTimestamp() - contactTimer) * 1e-9;
			if (contactTime == 0)
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
			driver.setDirection(initDir);
			driver.setSpeed(speed);
			double dribble = ((idx + Math.max(1, progress)) * 5000) + 1000;
			getMatchCtrl().setDribblerSpeed(dribble);
			
			if (!getTBot().hasBallContact() && (idx >= 2))
			{
				if (losttimer == 0)
				{
					losttimer = getWorldFrame().getTimestamp();
				} else if ((getWorldFrame().getTimestamp() - losttimer) > 0.5e9)
				{
					triggerEvent(EStateId.IDLE);
				}
			} else
			{
				losttimer = 0;
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.PUSH_BALL;
		}
		
	}
	
	
	/**
	 * @return
	 */
	public boolean isIdle()
	{
		return getCurrentState().getName().equals(EStateId.IDLE.toString());
	}
	
}
