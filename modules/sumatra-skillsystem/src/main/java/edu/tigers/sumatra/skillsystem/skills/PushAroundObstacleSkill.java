/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import static edu.tigers.sumatra.math.SumatraMath.relative;
import static java.lang.Math.abs;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PushAroundObstacleSkill extends AMoveSkill
{
	@Configurable(comment = "Push dist", defValue = "10.0")
	private static double pushDist = 10;
	@Configurable(comment = "Max velocity when pushing", defValue = "1.2")
	private static double pushVel = 1.2;
	@Configurable(comment = "Max acceleration when pushing", defValue = "0.8")
	private static double pushAcc = 0.8;
	@Configurable(comment = "dribbler speed for Pullback", defValue = "4000")
	private static int dribblerSpeed = 4000;
	
	
	private final DynamicPosition obstacle;
	private final DynamicPosition target;
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private final ExponentialMovingAverageFilter targetOrientationFilter = new ExponentialMovingAverageFilter(0.95);
	
	private enum EEvent implements IEvent
	{
		PUSH
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
		setInitialState(pushState);
		addTransition(EEvent.PUSH, pushState);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		ballStabilizer.update(getBall(), getTBot());
	}
	
	private class PushState extends AState
	{
		private double targetOrientation;
		private IVector2 desiredDestination;
		private TimestampTimer releaseBallTimer = new TimestampTimer(0.5);
		private int currentDribbleSpeed;
		
		
		@Override
		public void doEntryActions()
		{
			desiredDestination = null;
			currentDribbleSpeed = 0;
			
			targetOrientation = getBallPos().subtractNew(getPos()).getAngle();
			targetOrientationFilter.setState(targetOrientation);
			
			obstacle.setUseKickerPos(false);
			getMoveCon().getMoveConstraints().setVelMax(pushVel);
		}
		
		
		@Override
		public void doUpdate()
		{
			obstacle.update(getWorldFrame());
			target.update(getWorldFrame());
			ballStabilizer.setBotBrakeAcc(pushAcc);
			
			if (getVel().getLength2() <= getMoveCon().getMoveConstraints().getVelMax())
			{
				getMoveCon().getMoveConstraints().setAccMax(pushAcc);
			}
			
			desiredDestination = getIdealDestination();
			targetOrientation = calcTargetOrientation();
			
			AroundObstacleCalc aroundObstacleCalc = new AroundObstacleCalc(obstacle.getPos(), getBallPos(), getTBot());
			IVector2 dest = desiredDestination;
			
			if (target.getPos().distanceTo(getBallPos()) > 50)
			{
				if (aroundObstacleCalc.isAroundObstacleNeeded(dest))
				{
					dest = aroundObstacleCalc.getAroundObstacleDest().orElse(dest);
					targetOrientation = aroundObstacleCalc.adaptTargetOrientation(targetOrientation);
				}
				dest = aroundBall(dest);
				dest = aroundObstacleCalc.avoidObstacle(dest);
			} else
			{
				dest = LineMath.stepAlongLine(target.getPos(), desiredDestination,
						Geometry.getBallRadius() + getTBot().getCenter2DribblerDist());
			}
			
			setTargetPose(dest, targetOrientation);
			
			updateDribbler();
			
			getShapes().get(ESkillShapesLayer.PUSH_AROUND_OBSTACLE_SKILL)
					.add(new DrawablePoint(getBallPos(), Color.green));
			getShapes().get(ESkillShapesLayer.PUSH_AROUND_OBSTACLE_SKILL)
					.add(new DrawableBot(desiredDestination, targetOrientation, Color.green, 90,
							getTBot().getCenter2DribblerDist()));
			getShapes().get(ESkillShapesLayer.PUSH_AROUND_OBSTACLE_SKILL)
					.add(new DrawableBot(dest, targetOrientation, Color.red, 98, getTBot().getCenter2DribblerDist()));
		}
		
		
		private void updateDribbler()
		{
			if (target.getPos().distanceTo(getBallPos()) < 50)
			{
				releaseBallTimer.update(getWorldFrame().getTimestamp());
				if (releaseBallTimer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					currentDribbleSpeed = 0;
				}
			} else if (Math
					.abs(AngleMath.difference(getAngle(), target.getPos().subtractNew(getBallPos()).getAngle())) > 0.4)
			{
				currentDribbleSpeed = 0;
				releaseBallTimer.reset();
			} else
			{
				currentDribbleSpeed = dribblerSpeed;
				releaseBallTimer.reset();
			}
			getMatchCtrl().getSkill().getKickerDribbler().setDribblerSpeed(currentDribbleSpeed);
		}
		
		
		private IVector2 aroundBall(final IVector2 dest)
		{
			return AroundBallCalc
					.aroundBall()
					.withBallPos(getBallPos())
					.withTBot(getTBot())
					.withDestination(dest)
					.withMaxMargin(50)
					.withMinMargin(-getPushDist())
					.build()
					.getAroundBallDest();
		}
		
		
		private double getPushDist()
		{
			double dist2Target = getBallPos().distanceTo(target.getPos());
			if (getBallPos().distanceTo(getPos()) > Geometry.getBotRadius() + 50)
			{
				return Math.min(dist2Target, pushDist);
			}
			double requiredRotation = getRequiredRotation();
			double relRotation = 1 - relative(requiredRotation, 0, 0.7);
			double push = (relRotation * relRotation) * 500;
			return Math.min(dist2Target, push);
		}
		
		
		private double getRequiredRotation()
		{
			IVector2 ballToIdealDest = getBallPos().subtractNew(target.getPos());
			IVector2 ballToBot = getPos().subtractNew(getBallPos());
			return ballToIdealDest.angleToAbs(ballToBot).orElse(0.0);
		}
		
		
		private IVector2 getBallPos()
		{
			return ballStabilizer.getBallPos();
		}
		
		
		private IVector2 getIdealDestination()
		{
			double dist2Target = getBallPos().distanceTo(target.getPos());
			if (dist2Target < 50 && desiredDestination != null)
			{
				return desiredDestination;
			}
			double dist = Math.min(dist2Target, pushDist);
			return LineMath.stepAlongLine(getBallPos(), target.getPos(),
					-(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() - dist));
		}
		
		
		private double calcTargetOrientation()
		{
			IVector2 botAngleDirection = target.getPos().subtractNew(getBallPos());
			if (botAngleDirection.getLength2() < 50)
			{
				botAngleDirection = getBallPos().subtractNew(desiredDestination);
			}
			targetOrientationFilter.update(botAngleDirection.getAngle());
			double finalTargetOrientation = targetOrientationFilter.getState();
			
			if (getBall().getVel().getLength2() < 0.3)
			{
				double currentDirection = getBallPos().subtractNew(getPos()).getAngle(0);
				double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
				double relDiff = relative(abs(diff), 0.2, 0.8);
				
				double alteredDiff = relDiff * diff;
				
				return finalTargetOrientation - alteredDiff;
			}
			return finalTargetOrientation;
		}
	}
}
