/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.*;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PushAroundObstacleSkill extends AMoveSkill
{
	@Configurable(comment = "Push dist")
	private static double			pushDist	= 10;
	
	private final DynamicPosition	obstacle;
	private final DynamicPosition	target;
	
	@Configurable(comment = "Max velocity when pushing", defValue = "1.0")
	private static double			pushVel	= 1.0;
	@Configurable(comment = "Max acceleration when pushing", defValue = "1.0")
	private static double			pushAcc	= 1;
	
	
	private enum EEvent implements IEvent
	{
		PUSH,
		WAIT
	}
	
	
	/**
	 * @param obstacle
	 * @param target
	 */
	public PushAroundObstacleSkill(final DynamicPosition obstacle, final DynamicPosition target)
	{
		super(ESkill.PUSH_AROUND_OBSTACLE);
		this.obstacle = obstacle;
		this.target = target;
		
		IState pushState = new PushState();
		IState waitState = new WaitState();
		setInitialState(pushState);
		addTransition(EEvent.PUSH, pushState);
		addTransition(EEvent.WAIT, waitState);
	}
	
	
	private class PushState implements IState
	{
		@Override
		public void doUpdate()
		{
			if (target.distanceTo(getBall().getPos()) < 10)
			{
				return;
			}
			
			obstacle.update(getWorldFrame());
			target.update(getWorldFrame());
			obstacle.setUseKickerPos(false);
			getMoveCon().getMoveConstraints().setVelMax(pushVel);
			getMoveCon().getMoveConstraints().setAccMax(pushAcc);
			
			double targetOrientation = getTargetOrientation();
			IVector2 desiredDestination = getIdealDestination(0);
			
			AroundObstacleCalc aroundObstacleCalc = new AroundObstacleCalc(obstacle, getBall().getPos(), getTBot());
			IVector2 dest = desiredDestination;
			if (aroundObstacleCalc.isAroundObstacleNeeded(desiredDestination))
			{
				dest = aroundObstacleCalc.getAroundObstacleDest().orElse(dest);
				targetOrientation = aroundObstacleCalc.adaptTargetOrientation(targetOrientation);
			}
			dest = aroundBall(dest);
			dest = aroundObstacleCalc.avoidObstacle(dest);
			
			setTargetPose(dest, targetOrientation);
			
			getShapes().get(ESkillShapesLayer.PATH)
					.add(new DrawableBot(desiredDestination, targetOrientation, Color.green, 90,
							getTBot().getCenter2DribblerDist()));
			getShapes().get(ESkillShapesLayer.PATH)
					.add(new DrawableBot(dest, targetOrientation, Color.red, 98, getTBot().getCenter2DribblerDist()));
		}
		
		
		private IVector2 aroundBall(final IVector2 dest)
		{
			return AroundBallCalc
					.aroundBall()
					.withBallPos(getBall().getPos())
					.withTBot(getTBot())
					.withDestination(dest)
					.withMaxMargin(100)
					.withMinMargin(-getPushDist())
					.build()
					.getAroundBallDest();
		}
		
		
		private double getPushDist()
		{
			double dist2Target = getBallPos().distanceTo(target);
			return Math.min(dist2Target, pushDist);
		}
		
		
		private IVector2 getBallPos()
		{
			return getBall().getPos();
		}
		
		
		private IVector2 getIdealDestination(double margin)
		{
			
			return LineMath.stepAlongLine(getBallPos(), target, -getDistance(margin - getPushDist()));
		}
		
		
		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + margin;
		}
		
		
		private double getTargetOrientation()
		{
			return getBallPos().subtractNew(getTBot().getPos()).getAngle(0);
		}
	}
	
	private class WaitState implements IState
	{
		long tStart;
		
		
		@Override
		public void doEntryActions()
		{
			tStart = getWorldFrame().getTimestamp();
		}
		
		
		@Override
		public void doUpdate()
		{
			double tWaited = (getWorldFrame().getTimestamp() - tStart) / 1e9;
			if (tWaited > 0.1)
			{
				triggerEvent(EEvent.PUSH);
			}
		}
	}
}
