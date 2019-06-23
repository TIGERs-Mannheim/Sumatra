/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.throwin;

import java.awt.Color;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.BallPlacementSkill;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Kick the ball to the secondary bot if ball is further than push dist away from placement pos.
 * Else, clear area around placement area
 *
 * @author MarkG
 */
public class PrimaryPlacementRole extends APlacementRole
{
	/**
	 * Default
	 */
	public PrimaryPlacementRole()
	{
		super(ERole.PRIMARY_AUTOMATED_THROW_IN);
		
		setInitialState(new KickState());
		addTransition(EEvent.SHOOT, new KickState());
		addTransition(EEvent.PULL, new PullState());
		addTransition(EEvent.CLEAR, new ClearState());
	}
	
	
	private enum EEvent implements IEvent
	{
		SHOOT,
		PULL,
		CLEAR
	}
	
	private class ClearState implements IState
	{
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().getMoveConstraints().setAccMax(1);
			skill.getMoveCon().getMoveConstraints().setVelMax(1);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 dest = Vector2.zero();
			if (isFreeToPushBySecondRole())
			{
				dest = LineMath.stepAlongLine(getBall().getPos(), getPos(),
						OffensiveConstants.getAutomatedThrowInPushDistance() + Geometry.getBotRadius());
			} else if (getBall().getVel().getLength2() < 0.1)
			{
				triggerEvent(EEvent.SHOOT);
			}
			skill.getMoveCon().updateDestination(dest);
		}
		
		
		private boolean isFreeToPushBySecondRole()
		{
			return isInsidePushRadius() && (!isBallTooCloseToFieldBorder()
					|| !Geometry.getField().isPointInShape(getPlacementPos(), -300));
		}
	}
	
	
	private class PullState implements IState
	{
		private static final int GOAL_WALL_THICKNESS = 20;
		private BallPlacementSkill skill = null;
		private IRectangle goalRect = null;
		private IVector2 destination;
		
		
		@Override
		public void doEntryActions()
		{
			destination = getDestination();
			skill = new BallPlacementSkill(destination);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((!isBallTooCloseToFieldBorder()
					|| destination.distanceTo(getBall().getPos()) < Geometry.getBallRadius() * 2)
					&& getBall().getPos().distanceTo(getPos()) > 2 * Geometry.getBotRadius())
			{
				triggerEvent(EEvent.SHOOT);
			}
			if (goalRect != null)
			{
				getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.AUTOMATED_THROW_IN)
						.add(new DrawableRectangle(goalRect, Color.cyan));
			}
		}
		
		
		private IVector2 getDestination()
		{
			IVector2 ballPos = getBall().getPos();
			IVector2 dest;
			if (Geometry.getGoalOur().isPointInShape(ballPos, 200)
					|| Geometry.getPenaltyAreaOur().withMargin(-500).isBehindPenaltyArea(ballPos))
			{
				dest = getShapeAndDestination(Geometry.getGoalOur());
			} else if (Geometry.getGoalTheir().isPointInShape(ballPos, 200)
					|| Geometry.getPenaltyAreaTheir().withMargin(-500).isBehindPenaltyArea(ballPos))
			{
				dest = getShapeAndDestination(Geometry.getGoalTheir());
			} else
			{
				dest = Geometry.getField().nearestPointInside(ballPos, -2 * Geometry.getBotRadius());
			}
			return dest;
		}
		
		
		private IVector2 getShapeAndDestination(Goal goal)
		{
			goalRect = Rectangle.fromPoints(goal.getLeftPost(),
					goal.getRightPost().addNew(Vector2.fromX(Math.signum(goal.getCenter().x()) * goal.getDepth())));
			goalRect = goalRect.withMargin(200);
			return getDestinationOutsideGoal(goal);
		}
		
		
		private IVector2 getDestinationOutsideGoal(Goal goal)
		{
			IVector2 ballPos = getBall().getPos();
			if (isBallBesideGoal(goal))
			{
				IVector2 dest = Geometry.getField().nearestPointInside(ballPos, -Geometry.getBotRadius());
				return dest.addNew(Vector2.fromY(250 * Math.signum(ballPos.y())));
			}
			
			if (isBallBeforeGoalBackWall(goal))
			{
				return goal.getCenter().subtractNew(Vector2.fromX(250 * Math.signum(goal.getCenter().x())));
			}
			
			double distanceToGoalBackWall = goal.getDepth() + GOAL_WALL_THICKNESS + 2 * Geometry.getBotRadius();
			
			return Vector2.fromXY(goal.getCenter().x() + distanceToGoalBackWall * Math.signum(ballPos.x()),
					(goal.getWidth() / 2 + 2 * Geometry.getBotRadius()) * Math.signum(ballPos.y()));
		}
		
		
		private boolean isBallBesideGoal(Goal goal)
		{
			return Math.abs(getBall().getPos().y()) > goal.getWidth() / 2 + GOAL_WALL_THICKNESS;
		}
		
		
		private boolean isBallBeforeGoalBackWall(Goal goal)
		{
			return Math.abs(getBall().getPos().x()) < Math
					.abs(goal.getCenter().x() + goal.getDepth() * Math.signum(goal.getCenter().x()));
		}
	}
	
	
	private class KickState implements IState
	{
		private KickChillSkill skill;
		private TimestampTimer timer = new TimestampTimer(1);
		
		
		@Override
		public void doEntryActions()
		{
			
			DynamicPosition target = new DynamicPosition(
					Geometry.getField().nearestPointInside(getPlacementPos(), -Geometry.getBotRadius()));
			skill = new KickChillSkill(target);
			skill.setReadyForKick(false);
			skill.getMoveCon().setBotsObstacle(true);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (isBallTooCloseToFieldBorder() && getBall().getVel().getLength2() < 0.3)
			{
				triggerEvent(EEvent.PULL);
			} else if (getBall().getVel().getLength2() > 0.3 || isInsidePushRadius())
			{
				triggerEvent(EEvent.CLEAR);
			}
			
			
			double distToPlacementPos = getWFrame().getTigerBotsVisible().values().stream()
					.map(ITrackedBot::getPos)
					.map(pos -> pos
							.distanceTo(Geometry.getField().nearestPointInside(getPlacementPos(),
									-Geometry.getBotRadius())))
					.sorted()
					.findFirst()
					.orElse(0.0);
			if (distToPlacementPos < Geometry.getBotRadius() + 30)
			{
				if (isPathToTargetFree())
				{
					skill.setKickSpeed(getKickSpeed());
					skill.setReadyForKick(true);
				} else
				{
					timer.update(getWFrame().getTimestamp());
					if (timer.isTimeUp(getWFrame().getTimestamp()))
					{
						// do not kick with more than 1.5m/s
						// the rules state that if a ball hits an opponent robot with >1.5m/s, its a foul
						skill.setKickSpeed(Math.min(1.5, getKickSpeed()));
						skill.setReadyForKick(true);
					}
				}
			} else
			{
				skill.setReadyForKick(false);
				timer.reset();
			}
		}
		
		
		private double getKickSpeed()
		{
			double dist = getPlacementPos().distanceTo(getBall().getPos());
			return getBall().getStraightConsultant().getInitVelForDist(dist, 1);
		}
		
		
		private boolean isPathToTargetFree()
		{
			return AiMath.p2pVisibility(getWFrame().getBots().values(), getBall().getPos(), getPlacementPos(), 10.0);
		}
	}
}
