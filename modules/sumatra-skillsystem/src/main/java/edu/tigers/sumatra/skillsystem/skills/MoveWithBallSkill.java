/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


/**
 * Move with the ball on the dribbler.<br>
 * Assumptions:
 * <ul>
 *    <li>Ball is already on dribbler and barrier is interrupted</li>
 *    <li>Bot is capable of rotating with ball on dribbler</li>
 * </ul>
 */
@Log4j2
public class MoveWithBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Min contact time [s] to reach before moving with ball", defValue = "0.3")
	private static double minContactTime = 0.3;

	@Configurable(comment = "How fast to accelerate when rotating with ball", defValue = "10.0")
	private static double rotationWithBallVel = 10.0;

	@Configurable(comment = "How fast to accelerate when rotating with ball", defValue = "10.0")
	private static double rotationWithBallAcc = 10.0;

	@Configurable(comment = "How fast to drive when moving with ball", defValue = "3.0")
	private static double moveWithBallVel = 3.0;

	@Configurable(comment = "How fast to accelerate when moving with ball", defValue = "2.0")
	private static double moveWithBallAcc = 2.0;

	@Configurable(comment = "How fast to drive when pushing the ball forward", defValue = "1.5")
	private static double moveWithBallFwdVel = 1.5;

	@Configurable(comment = "How fast to accelerate when pushing the ball forward", defValue = "1.0")
	private static double moveWithBallFwdAcc = 1.0;

	@Configurable(comment = "Dribbler speed", defValue = "10000.0")
	private static double dribblerSpeed = 10000;

	@Configurable(comment = "Dribbler speed when pushing ball", defValue = "5000.0")
	private static double dribblerSpeedPush = 5000;

	@Configurable(defValue = "true", comment = "If false, the ball is always pulled, turning with the ball by 180° if necessary")
	private static boolean allowPushingTheBall = true;

	private final TimestampTimer calmDownTimer = new TimestampTimer(0.5);

	@Setter
	private IVector2 finalDest = Vector2.zero();
	@Setter
	private Double finalOrientation;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();

		getMoveCon().physicalObstaclesOnly();
		getMoveCon().setBallObstacle(false);
		getMoveConstraints().setVelMaxW(rotationWithBallVel);
		getMoveConstraints().setAccMaxW(rotationWithBallAcc);

		updateTargetAngle(getAngle());
	}


	@Override
	public void doUpdate()
	{
		updateTargetAngle(calcTargetAngle());
		updateDestination(calcDestination());

		if (driveForward())
		{
			getMoveConstraints().setVelMax(moveWithBallFwdVel);
			getMoveConstraints().setAccMax(moveWithBallFwdAcc);
			if (calmDownTimer.isRunning() && getDestinationReachedIn() < 0.1)
			{
				setKickParams(KickParams.disarm().withDribbleSpeed(0.0));
			} else
			{
				setKickParams(KickParams.disarm().withDribbleSpeed(dribblerSpeedPush));
			}
		} else
		{
			getMoveConstraints().setAccMax(moveWithBallAcc);
			getMoveConstraints().setVelMax(moveWithBallVel);
			setKickParams(KickParams.disarm().withDribbleSpeed(dribblerSpeed));
		}

		super.doUpdate();

		if (ballAtTarget())
		{
			calmDownTimer.update(getWorldFrame().getTimestamp());
		} else
		{
			calmDownTimer.reset();
		}

		if (!getTBot().getBallContact().hadContact(minContactTime)
				&& !getTBot().getBallContact().isBallContactFromVision())
		{
			setSkillState(ESkillState.FAILURE);
		} else if (calmDownTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			setSkillState(ESkillState.SUCCESS);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}

		getShapes().get(ESkillShapesLayer.MOVE_WITH_BALL)
				.add(new DrawableBotShape(finalDest, finalOrientation(), Geometry.getBotRadius() + 20,
						getBot().getCenter2DribblerDist()));
	}


	private double finalOrientation()
	{
		return finalOrientation == null ? getTargetAngle() : finalOrientation;
	}


	private IVector2 calcDestination()
	{
		if (Math.abs(AngleMath.difference(getAngle(), getTargetAngle())) < 0.3
				&& getTBot().getBallContact().hasContactFromVisionOrBarrier())
		{
			// start moving only after target angle reached approximately
			return finalDest;
		}
		return getPos();
	}


	private boolean driveForward()
	{
		double forwardAngle = finalDest.subtractNew(getPos()).getAngle(getAngle());
		return allowPushingTheBall && AngleMath.diffAbs(forwardAngle, getAngle()) < AngleMath.DEG_090_IN_RAD;
	}


	private double calcTargetAngle()
	{
		if (finalDest.distanceTo(getPos()) > 50)
		{
			double forwardAngle = finalDest.subtractNew(getPos()).getAngle();
			if (driveForward())
			{
				return forwardAngle;
			}
			return forwardAngle + AngleMath.DEG_180_IN_RAD;
		} else if (finalOrientation != null)
		{
			return finalOrientation;
		}
		return getTargetAngle();
	}


	private boolean ballAtTarget()
	{
		var destDiff = getPos().distanceTo(finalDest);
		var angleDiff = Math.abs(AngleMath.difference(getAngle(), finalOrientation()));
		return destDiff < 50 && angleDiff < 0.1;
	}
}
