/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.throwin;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.PushAroundObstacleSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Place the ball to the ball placement position by either receiving the ball from a secondary role if present or by
 * pushing the ball to the target
 */
public class PrimaryBallPlacementRole extends ABallPlacementRole
{
	private final ClearState clear = new ClearState();
	
	
	public PrimaryBallPlacementRole()
	{
		super(ERole.PRIMARY_BALL_PLACEMENT);
		final ReceiveState receive = new ReceiveState();
		final PushState push = new PushState();
		final PullState pull = new PullState();
		final PreparePushState preparePush = new PreparePushState();
		final StopBallState stopBall = new StopBallState();
		
		setInitialState(stopBall);
		
		addTransition(EEvent.RECEIVE, receive);
		addTransition(EEvent.PUSH, push);
		addTransition(EEvent.CLEAR, clear);
		addTransition(EEvent.PULL, pull);
		addTransition(EEvent.PREPARE_PUSH, preparePush);
		addTransition(EEvent.STOP_BALL, stopBall);
		addTransition(pull, EEvent.DONE, clear);
	}
	
	
	@SuppressWarnings("unused") // used by UI
	public PrimaryBallPlacementRole(final IVector2 placementPos)
	{
		this();
		setPlacementPos(placementPos);
	}
	
	
	public boolean isBallCleared()
	{
		return getCurrentState() == clear && clear.isDestReached();
	}
	
	private class ClearState extends ClearBaseState
	{
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			if (!isBallAtTarget())
			{
				if (isBallLying())
				{
					if (isBallInsidePushRadius() || !hasCompanion())
					{
						triggerEvent(EEvent.PUSH);
					} else
					{
						triggerEvent(EEvent.RECEIVE);
					}
				} else if (getBall().getVel().getLength2() > 0.4)
				{
					triggerEvent(EEvent.STOP_BALL);
				}
			}
		}
	}
	
	private class ReceiveState extends AState
	{
		private ReceiveBallSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new ReceiveBallSkill(getPlacementPos());
			setNewSkill(skill);
			prepareMoveCon(skill.getMoveCon());
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((!isSecondaryRoleNeeded() && getBall().getVel().getLength2() < 0.7)
					|| (skill.ballHasBeenReceived() && isBallAtTarget()))
			{
				triggerEvent(EEvent.CLEAR);
			}
		}
	}
	
	private class PreparePushState extends AState
	{
		private AMoveSkill skill = AMoveToSkill.createMoveToSkill();
		private TimestampTimer destReachedTimer = new TimestampTimer(0.3);
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			prepareMoveCon(skill.getMoveCon());
			skill.getMoveCon().updateLookAtTarget(getBall());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 dest = LineMath.stepAlongLine(getBall().getPos(), getPlacementPos(), -Geometry.getBotRadius() * 3);
			skill.getMoveCon().updateDestination(dest);
			
			if (dest.distanceTo(getPos()) < 50)
			{
				destReachedTimer.update(getWFrame().getTimestamp());
			} else
			{
				destReachedTimer.reset();
			}
			
			if (!isBallInsidePushRadius() && hasCompanion())
			{
				triggerEvent(EEvent.RECEIVE);
			} else if (!Geometry.getField().isPointInShape(getBall().getPos()))
			{
				triggerEvent(EEvent.PULL);
			} else if (destReachedTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				triggerEvent(EEvent.PUSH);
			} else if (getBall().getPos().distanceTo(getPlacementPos()) < 80)
			{
				triggerEvent(EEvent.CLEAR);
			}
		}
	}
	
	private class PushState extends AState
	{
		private final TimestampTimer ballPlacedWaitTimer = new TimestampTimer(0.5);
		
		
		@Override
		public void doEntryActions()
		{
			DynamicPosition obstacle = new DynamicPosition(Vector2.fromXY(99999, 99999));
			DynamicPosition target = new DynamicPosition(getPlacementPos());
			PushAroundObstacleSkill skill = new PushAroundObstacleSkill(obstacle, target);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (isBallTooCloseToFieldBorder(50))
			{
				triggerEvent(EEvent.PULL);
			} else if (!isNearBall(RuleConstraints.getStopRadius() + 50))
			{
				triggerEvent(EEvent.PREPARE_PUSH);
			} else if (getPlacementPos().distanceTo(getBall().getPos()) > 200
					&& getBall().getPos().distanceTo(getBot().getBotKickerPos()) > 150
					&& !getBot().hasBallContact()
					&& getBall().getVel().angleToAbs(getPlacementPos().subtractNew(getBall().getPos()))
							.orElse(0.0) > AngleMath.PI_QUART)
			{
				triggerEvent(EEvent.STOP_BALL);
			} else if (isBallAtTarget())
			{
				ballPlacedWaitTimer.update(getWFrame().getTimestamp());
				if (ballPlacedWaitTimer.isTimeUp(getWFrame().getTimestamp()))
				{
					triggerEvent(EEvent.CLEAR);
				}
			} else
			{
				ballPlacedWaitTimer.reset();
			}
		}
		
		
		private boolean isNearBall(final double radius)
		{
			return getPos().distanceTo(getBall().getPos()) < radius;
		}
	}
	
	private class StopBallState extends AState
	{
		private final TimestampTimer ballStoppedWaitTimer = new TimestampTimer(0.2);
		
		
		@Override
		public void doEntryActions()
		{
			final ApproachAndStopBallSkill skill = new ApproachAndStopBallSkill();
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
			ballStoppedWaitTimer.start(1);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallInsidePushRadius() && hasCompanion())
			{
				triggerEvent(EEvent.RECEIVE);
			} else if (getBall().getVel().getLength2() < 0.2)
			{
				ballStoppedWaitTimer.update(getWFrame().getTimestamp());
				if (ballStoppedWaitTimer.isTimeUp(getWFrame().getTimestamp()))
				{
					triggerEvent(EEvent.PUSH);
				}
			} else
			{
				ballStoppedWaitTimer.reset();
			}
		}
	}
}
