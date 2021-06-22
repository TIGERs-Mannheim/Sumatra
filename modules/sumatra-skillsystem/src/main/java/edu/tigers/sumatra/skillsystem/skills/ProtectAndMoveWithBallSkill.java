/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;

import java.awt.Color;


/**
 * Protect the ball against a given opponent
 */
public class ProtectAndMoveWithBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Dribbler speed while protecting", defValue = "5000.0")
	private static double protectDribbleSpeed = 5000;

	@Setter
	IVector2 protectionTarget;

	private TimestampTimer changeStateTimer = new TimestampTimer(0.1);

	private IVector3 endPose = null;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		getMoveConstraints().setAccMax(3.0);
		getMoveConstraints().setVelMax(1.5);
		getShapes().get(ESkillShapesLayer.PROTECT_AND_MOVE_WITH_BALL_SKILL)
				.add(new DrawableArrow(getPos(), protectionTarget.subtractNew(getPos())));

		IVector2 tigerToBall = Vector2.fromAngle(getAngle());
		IVector2 enemyToBall = getBall().getPos().subtractNew(protectionTarget);
		double attackAngle = tigerToBall.angleTo(enemyToBall).orElse(0.0);

		double distEnemyToBall = enemyToBall.getLength();
		double turnRadius = 500 * (1 / (Math.abs(attackAngle) + 1e-5));
		double dist = 5 + (150 * (Math.abs(attackAngle) / Math.PI)) * (1 - (Math.min(2000, distEnemyToBall) / 2000));
		double angle = AngleMath.deg2rad((dist * 180) / (Math.PI * turnRadius)) * (Math.abs(attackAngle) / Math.PI);

		ICircle circle;
		if (attackAngle > 0)
		{
			circle = Circle
					.createCircle(
							getPos().addNew(Vector2.fromAngle(getAngle()).getNormalVector().scaleToNew(-turnRadius)),
							turnRadius);
		} else
		{
			circle = Circle
					.createCircle(getPos().addNew(Vector2.fromAngle(getAngle()).getNormalVector().scaleToNew(turnRadius)),
							turnRadius);
			angle = -angle;
		}

		IVector2 carrotTarget = CircleMath.stepAlongCircle(getPos(), circle.center(), angle);
		getShapes().get(ESkillShapesLayer.PROTECT_AND_MOVE_WITH_BALL_SKILL).add(new DrawableCircle(circle, Color.red));
		if (Math.abs(attackAngle) < AngleMath.deg2rad(20) && dist < 30)
		{
			if (endPose == null)
			{
				endPose = Vector3.fromXYZ(getPos().x(), getPos().y(), getAngle());
			}
			updateDestination(endPose.getXYVector());
			updateTargetAngle(endPose.z());
		} else
		{
			endPose = null;
			updateDestination(carrotTarget);
			updateLookAtTarget(carrotTarget);
		}

		if (!getTBot().hasBallContact())
		{
			if (!changeStateTimer.isRunning())
			{
				changeStateTimer.start(getWorldFrame().getTimestamp());
			}
			if (changeStateTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				setSkillState(ESkillState.FAILURE);
			}
		} else
		{
			changeStateTimer.reset();
		}

		setKickParams(KickParams.disarm().withDribbleSpeed(protectDribbleSpeed));
		super.doUpdate();
	}
}
