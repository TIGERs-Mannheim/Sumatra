/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;

import java.awt.Color;


/**
 * Protect the ball against a given opponent
 */
public class ProtectAndMoveWithBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Carrot movement distance multiplier", defValue = "1.8")
	private static double carrotMovementMultiplier = 1.8;

	@Configurable(comment = "Carrot movement base distance [mm]", defValue = "5.0")
	private static double carrotMovementBaseValue = 5.0;

	@Configurable(comment = "Speed bonus attack Angle multiplier", defValue = "180.0")
	private static double speedBonusAttackAngleMultiplier = 180.0;

	@Configurable(comment = "Speed bonus opponentDist upper distance", defValue = "2000.0")
	private static double speedBonusOpponentDistUpperLimit = 2000.0;

	@Setter
	IVector2 protectionTarget;

	private TimestampTimer changeStateTimer = new TimestampTimer(0.1);

	private IVector3 endPose = null;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setBotsObstacle(false);
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
		double dist = calculateCarrotMovementDistance(attackAngle, distEnemyToBall);

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

		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
		super.doUpdate();
	}


	private double calculateCarrotMovementDistance(double attackAngle, double distEnemyToBall)
	{
		double speedBonusAttackAngle = (speedBonusAttackAngleMultiplier * (Math.abs(attackAngle) / AngleMath.PI));
		double speedBonusOpponentDist = SumatraMath.relative(distEnemyToBall, speedBonusOpponentDistUpperLimit, 0);
		return carrotMovementBaseValue + speedBonusAttackAngle * speedBonusOpponentDist * carrotMovementMultiplier;
	}
}
