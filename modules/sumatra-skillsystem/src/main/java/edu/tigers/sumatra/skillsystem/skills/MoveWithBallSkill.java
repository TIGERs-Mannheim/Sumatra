/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
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
	@Configurable(comment = "Min contact time [s] to reach before moving with ball", defValue = "0.2")
	private static double minContactTime = 0.2;

	@Configurable(comment = "How fast to accelerate when rotating with ball", defValue = "10.0")
	private static double rotationWithBallVel = 10.0;

	@Configurable(comment = "How fast to accelerate when rotating with ball", defValue = "10.0")
	private static double rotationWithBallAcc = 10.0;

	@Configurable(comment = "How fast to drive when moving with ball", defValue = "1.5")
	private static double moveWithBallVel = 1.5;

	@Configurable(comment = "How fast to accelerate when moving with ball", defValue = "1.0")
	private static double moveWithBallAcc = 1.0;

	@Configurable(comment = "How fast to drive when pushing the ball forward", defValue = "1.5")
	private static double moveWithBallFwdVel = 1.5;

	@Configurable(comment = "How fast to accelerate when pushing the ball forward", defValue = "1.0")
	private static double moveWithBallFwdAcc = 1.0;

	@Getter
	@Configurable(defValue = "true", comment = "If false, the ball is always pulled, turning with the ball by 180° if necessary")
	private static boolean allowPushingTheBall = true;

	@Configurable(defValue = "200", comment = "Min distance [mm] to final destination to update target angle")
	private static double minDistanceToFinalDestToUpdateTargetAngle = 200;

	@Configurable(comment = "Factor that is applied on the default dribble force when pushing the ball", defValue = "0.25")
	private static double forwardDribbleForceFactor = 0.25;

	@Configurable(comment = "Offset [m/s] that is added to the default dribble force when pushing the ball", defValue = "0.0")
	private static double forwardDribbleForceOffset = 0.0;

	private final TimestampTimer calmDownTimer = new TimestampTimer(0.5);

	@Setter
	private IVector2 finalDest = Vector2.zero();
	@Setter
	private Double finalOrientation;
	@Setter
	private double forcedChipSpeed = 0;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();

		getMoveCon().physicalObstaclesOnly();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setFieldBorderObstacle(false);
		getMoveCon().setGoalPostsObstacle(false);
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
			setKickParams(getKick().withDribbleSpeed(
					getBot().getBotParams().getDribblerSpecs().getDefaultSpeed(),
					getBot().getBotParams().getDribblerSpecs().getDefaultForce() * forwardDribbleForceFactor
							+ forwardDribbleForceOffset
					)
			);
		} else
		{
			getMoveConstraints().setAccMax(moveWithBallAcc);
			getMoveConstraints().setVelMax(moveWithBallVel);
			setKickParams(getKick().withDribblerMode(EDribblerMode.DEFAULT));
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
				&& !getTBot().getBallContact().hasContactFromVision())
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


	private KickParams getKick()
	{
		if (SumatraMath.isEqual(0, forcedChipSpeed))
		{
			return KickParams.disarm();
		}
		return KickParams.chip(forcedChipSpeed);
	}


	private double finalOrientation()
	{
		return finalOrientation == null ? getTargetAngle() : finalOrientation;
	}


	private IVector2 calcDestination()
	{
		if (Math.abs(AngleMath.difference(getAngle(), getTargetAngle())) < 0.3
				&& (getTBot().getBallContact().hadContact(minContactTime))
				|| getTBot().getBallContact().hasContactFromVisionOrBarrier())
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
		if (finalDest.distanceTo(getPos()) > minDistanceToFinalDestToUpdateTargetAngle)
		{
			double forwardAngle = finalDest.subtractNew(getBall().getPos()).getAngle();
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
