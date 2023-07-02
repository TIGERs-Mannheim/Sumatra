/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter2D;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import lombok.Setter;

import java.awt.Color;

import static edu.tigers.sumatra.math.SumatraMath.relative;
import static java.lang.Math.abs;


/**
 * Push the ball forward, optionally circumventing an obstacle
 */
public class PushAroundObstacleSkill extends AMoveSkill
{
	@Configurable(comment = "Push dist", defValue = "50.0")
	private static double pushDist = 50;
	@Configurable(comment = "Max velocity when pushing", defValue = "2.0")
	private static double pushVel = 2.0;
	@Configurable(comment = "Max acceleration when pushing", defValue = "1.0")
	private static double pushAcc = 1.0;

	private final ExponentialMovingAverageFilter2D targetOrientationFilter = new ExponentialMovingAverageFilter2D(0.95);
	private final ExponentialMovingAverageFilter pushFilter = new ExponentialMovingAverageFilter(0.99);
	private final TimestampTimer releaseBallTimer = new TimestampTimer(0.3);
	private final TimestampTimer releasedBallTimer = new TimestampTimer(0.2);

	@Setter
	private DynamicPosition obstacle = new DynamicPosition(Vector2.fromXY(99999, 99999));
	@Setter
	private DynamicPosition target;

	private double targetOrientation;
	private IVector2 desiredDestination;
	private EDribblerMode currentDribbleMode;
	private MoveConstraints moveConstraints;


	@Override
	public void doEntryActions()
	{
		desiredDestination = null;
		currentDribbleMode = EDribblerMode.DEFAULT;
		pushFilter.setState(0);

		targetOrientation = getBall().getPos().subtractNew(getPos()).getAngle();
		targetOrientationFilter.setState(getBall().getPos().subtractNew(getPos()).normalize());

		moveConstraints = defaultMoveConstraints();
		moveConstraints.setVelMax(pushVel);
	}


	@Override
	public void doUpdate()
	{
		obstacle = obstacle.update(getWorldFrame());
		target = target.update(getWorldFrame());

		if (getVel().getLength2() <= moveConstraints.getVelMax())
		{
			moveConstraints.setAccMax(pushAcc);
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

		setTargetPose(dest, targetOrientation, moveConstraints);

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
				currentDribbleMode = EDribblerMode.OFF;
				releasedBallTimer.update(getWorldFrame().getTimestamp());
			} else
			{
				releasedBallTimer.reset();
			}
		} else
		{
			currentDribbleMode = EDribblerMode.DEFAULT;
			releaseBallTimer.reset();
			releasedBallTimer.reset();
		}
		setKickParams(KickParams.disarm().withDribblerMode(currentDribbleMode));
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
		double cappedPushDist = Math.min(dist2Target, push);
		if (cappedPushDist > pushFilter.getState())
		{
			pushFilter.update(cappedPushDist);
		} else
		{
			pushFilter.setState(cappedPushDist);
		}
		return pushFilter.getState();
	}


	private double getRequiredRotation()
	{
		IVector2 ballToIdealDest = getBallPos().subtractNew(target.getPos());
		IVector2 ballToBot = getPos().subtractNew(getBallPos());
		return ballToIdealDest.angleToAbs(ballToBot).orElse(0.0);
	}


	private IVector2 getBallPos()
	{
		return getBall().getPos();
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
		targetOrientationFilter.update(botAngleDirection.normalizeNew());
		double finalTargetOrientation = targetOrientationFilter.getState().getXYVector().getAngle();

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
