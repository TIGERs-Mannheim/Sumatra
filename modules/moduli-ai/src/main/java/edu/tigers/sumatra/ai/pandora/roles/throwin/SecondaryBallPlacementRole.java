/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.throwin;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Kick the ball to the primary bot if ball is further than push dist away from placement pos.
 * Else, clear area around placement area
 */
public class SecondaryBallPlacementRole extends ABallPlacementRole
{
	@Configurable(defValue = "2.0")
	private static double passEndVel = 2.0;
	
	
	/**
	 * Default
	 */
	public SecondaryBallPlacementRole()
	{
		super(ERole.SECONDARY_BALL_PLACEMENT);
		
		final PassState kick = new PassState();
		final ClearState clear = new ClearState();
		final PullSecondaryState pull = new PullSecondaryState();
		
		setInitialState(kick);
		
		addTransition(EEvent.PASS, kick);
		addTransition(EEvent.PULL, pull);
		addTransition(pull, EEvent.DONE, clear);
		addTransition(EEvent.CLEAR, clear);
	}
	
	
	@SuppressWarnings("unused") // used by reflection
	public SecondaryBallPlacementRole(final IVector2 placementPos)
	{
		this();
		setPlacementPos(placementPos);
	}
	
	private class ClearState extends ClearBaseState
	{
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			awayFromBallMover.setMinDistanceToBall(RuleConstraints.getStopRadius() + 300);
		}
		
		
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			if (isPassToPrimary())
			{
				triggerEvent(EEvent.PASS);
			}
		}
		
		
		private boolean isPassToPrimary()
		{
			return isSecondaryRoleNeeded() && ballIsLying();
		}
	}
	
	private class PullSecondaryState extends PullState
	{
		@Override
		public void doUpdate()
		{
			if (!isSecondaryRoleNeeded())
			{
				triggerEvent(EEvent.CLEAR);
			}
			super.doUpdate();
		}
	}
	
	
	private class PassState extends AState
	{
		private SingleTouchKickSkill skill;
		private TimestampTimer timer = new TimestampTimer(1);
		
		
		@Override
		public void doEntryActions()
		{
			DynamicPosition target = new DynamicPosition(
					Geometry.getField().nearestPointInside(getPlacementPos(), -Geometry.getBotRadius()));
			double distance = target.getPos().distanceTo(getBall().getPos());
			double kickSpeed = getBall().getStraightConsultant().getInitVelForDist(distance, passEndVel);
			skill = new SingleTouchKickSkill(target, KickParams.straight(kickSpeed));
			skill.setReadyForKick(false);
			skill.getMoveCon().setBotsObstacle(true);
			skill.getMoveCon().setGoalPostObstacle(true);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isSecondaryRoleNeeded())
			{
				triggerEvent(EEvent.CLEAR);
			} else if (pullIsRequired() && ballIsLying())
			{
				triggerEvent(EEvent.PULL);
			} else if (!ballIsLying() || isBallInsidePushRadius())
			{
				triggerEvent(EEvent.CLEAR);
			}
			
			double distToPlacementPos = getClosestDistToPlacementPos();
			if (distToPlacementPos < Geometry.getBotRadius())
			{
				if (isPathToTargetFree())
				{
					skill.setReadyForKick(true);
				} else
				{
					timer.update(getWFrame().getTimestamp());
					if (timer.isTimeUp(getWFrame().getTimestamp()))
					{
						skill.setReadyForKick(true);
					}
				}
			} else
			{
				skill.setReadyForKick(false);
				timer.reset();
			}
		}
		
		
		private Double getClosestDistToPlacementPos()
		{
			return getWFrame().getTigerBotsVisible().values().stream()
					.map(ITrackedBot::getBotKickerPos)
					.map(pos -> pos.distanceTo(getPlacementPos()) - Geometry.getBallRadius())
					.sorted()
					.findFirst()
					.orElse(0.0);
		}
		
		
		private boolean isPathToTargetFree()
		{
			return AiMath.p2pVisibility(getWFrame().getBots().values(), getBall().getPos(), getPlacementPos(),
					Geometry.getBallRadius(), getBotID());
		}
	}
}
