/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.Color;


/**
 * The 'normal' kick skill for in-game kicks.
 */
@NoArgsConstructor
public class TouchKickSkill extends ATouchKickSkill
{
	@Configurable(defValue = "HIGH_POWER")
	private static EDribblerMode dribblerMode = EDribblerMode.HIGH_POWER;

	@Configurable(defValue = "20", comment = "Maximum angular velocity [rad/s] for this skill")
	private static double maxAngularVelocity = 20;

	@Configurable(defValue = "2", comment = "Maximum acceleration [m/s²] when having ball contact")
	private static double ballContactAccMax = 2;

	@Configurable(defValue = "25.0", comment = "Maximum rotation acc [rad/s²] when having ball contact")
	private static double ballContactAccMaxW = 25.0;

	@Configurable(defValue = "0.047", comment = "Lookahead [s] on the orientation for aiming")
	private static double orientationLookaheadKickerArm = 0.047;

	@Configurable(defValue = "3.0", comment = "[deg]")
	private static double targetReachedArmEarlyOverstepCheck = 3.0;

	@Configurable(defValue = "0.01", comment = "Lookahead [s] on the orientation for aiming for simulation")
	private static double orientationLookaheadKickerArmSimulation = 0.01;

	@Configurable(defValue = "true", comment = "Arm kicker early to compensate the rotation during the transmission delay")
	private static boolean enableArmEarlyReality = true;

	@Configurable(defValue = "true", comment = "Same as enableArmEarlyReality, but this configurable is used while simulating")
	private static boolean enableArmEarlySimulation = true;

	@Configurable(defValue = "true", comment = "Enable kick direction compensation")
	private static boolean enableKickDirectionCompensation = true;

	private double maxBallSpeedThreshold;

	private IVector2 initBallPos;

	private TimestampTimer keepKickerArmedTimer = new TimestampTimer(0.1);

	@Setter
	private boolean forcePushDuringKick = false;


	public TouchKickSkill(final IVector2 target, final KickParams desiredKickParams)
	{
		this.target = target;
		this.desiredKickParams = desiredKickParams;
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		initBallPos = getBall().getPos();
		maxBallSpeedThreshold = Math.max(maxBallSpeed, getBall().getVel().getLength2() + 0.1);
		keepKickerArmedTimer.reset();
	}


	@Override
	public void doUpdate()
	{
		if (getTBot().getBallContact().hadContact(0.1))
		{
			getMoveConstraints().setAccMax(ballContactAccMax);
			getMoveConstraints().setAccMaxW(ballContactAccMaxW);
		} else
		{
			getMoveConstraints().resetLimits(getBot().getBotParams().getMovementLimits());
		}
		getMoveConstraints().setVelMaxW(maxAngularVelocity);

		if (armKicker())
		{
			setKickParams(getArmedKickParams().withDribblerMode(dribblerMode));

			if (forcePushDuringKick)
			{
				// we want to accelerate forward if we are ready to kick!
				updateDestination(target);
			}
		} else
		{
			setKickParams(KickParams.disarm().withDribblerMode(dribblerMode));
		}

		// this will perform the move based on the currently set Destination
		super.doUpdate();

		if (initBallPos.distanceTo(getBall().getPos()) > 500)
		{
			setSkillState(ESkillState.FAILURE);
		} else if (getBall().getVel().getLength2() > maxBallSpeedThreshold && !getTBot().getBallContact().hasContact())
		{
			if (AngleMath.diffAbs(getBall().getVel().getAngle(), targetOrientation) < AngleMath.DEG_045_IN_RAD)
			{
				setSkillState(ESkillState.SUCCESS);
			} else
			{
				setSkillState(ESkillState.FAILURE);
			}
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
	}


	private boolean armKicker()
	{
		boolean armEarly = armKickerEarly();
		if (armEarly)
		{
			// starting timer
			keepKickerArmedTimer.start(getWorldFrame().getTimestamp());
		}

		return isFocused() || armEarly || (keepKickerArmedTimer.isRunning() && !keepKickerArmedTimer.isTimeUp(
				getWorldFrame().getTimestamp()));
	}


	private double getCompensatedKickOrientation()
	{
		if (!enableKickDirectionCompensation)
		{
			return finalTargetOrientation;
		}

		// factors that influence outgoing bal vel:
		// Current ball velocity, which is equal to the tangential velocity of the robots rotation on location of the ball,
		// plus the robots velocity
		var robotVel = getVel(); // m/s
		double radius = getTBot().getCenter2DribblerDist() + Geometry.getBallRadius(); // mm
		var ballVel = Vector2.fromAngleLength(
				Vector2.fromAngle(getTBot().getOrientation()).getNormalVector().getAngle(),
				-getTBot().getAngularVel() * radius / 1000.0
		);
		var compensationVel = robotVel.addNew(ballVel);

		getShapes().get(ESkillShapesLayer.KICK_SKILL_COMP)
				.add(new DrawableArrow(getBallPos(), compensationVel.multiplyNew(1000), Color.magenta));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_COMP)
				.add(new DrawableArrow(getBallPos(), ballVel.multiplyNew(1000), Color.BLACK));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_COMP)
				.add(new DrawableArrow(getBallPos(), robotVel.multiplyNew(1000), Color.GRAY));

		var ballOffset = getTBot().getBotKickerPos().subtractNew(getTBot().getPos())
				.scaleToNew(Geometry.getBallRadius());

		// basically angle from ball to target
		double plannedBallVelAngle = target.subtractNew(getTBot().getBotKickerPos().addNew(ballOffset)).getAngle();

		IVector2 plannedKick = Vector2.fromAngleLength(plannedBallVelAngle, getKickSpeed());
		IVector2 kick = plannedKick.subtractNew(compensationVel);

		getShapes().get(ESkillShapesLayer.KICK_SKILL_COMP)
				.add(new DrawableArrow(getBallPos(), kick.multiplyNew(1000), Color.ORANGE.darker()));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_COMP)
				.add(new DrawableArrow(
						getBallPos(),
						Vector2.fromAngle(getAngle()).scaleToNew(kick.multiplyNew(1000).getLength()),
						Color.ORANGE.brighter()
				));
		return kick.getAngle();
	}


	private boolean armKickerEarly()
	{
		double orientationLookahead;
		if (SumatraModel.getInstance().isSimulation())
		{
			orientationLookahead = orientationLookaheadKickerArmSimulation;
			if (!enableArmEarlySimulation)
			{
				return false;
			}
		} else
		{
			orientationLookahead = orientationLookaheadKickerArm;
			if (!enableArmEarlyReality)
			{
				return false;
			}
		}

		double compensatedFinalTargetOrientation = getCompensatedKickOrientation();

		double orientation = getTBot().getOrientation();
		double orientationChange = getTBot().getAngularVel() * orientationLookahead;

		// use the uncompensated Orientation on purpose
		if (!isRoughlyTargeted(finalTargetOrientation))
		{
			// avoid overflow in the opposite direction
			return false;
		}

		// We do not use getAngleByTime on the Trajectory because it would perfectly converge with the
		// target orientation. However, we need to detect whether the angle diff is positive or negative. b
		double orientationFuture = orientation + orientationChange;
		getShapes().get(ESkillShapesLayer.KICK_SKILL_COMP)
				.add(new DrawableArrow(
						getBallPos(),
						Vector2.fromAngle(orientationFuture).scaleToNew(getBallPos().subtractNew(target).getLength()),
						Color.ORANGE.brighter()
				));

		double futureDif = AngleMath.difference(compensatedFinalTargetOrientation, orientationFuture);
		double angleDif = AngleMath.difference(compensatedFinalTargetOrientation, orientation);

		boolean overstepped = Math.signum(getTBot().getAngularVel()) == Math.signum(angleDif) &&
				Math.signum(getTBot().getAngularVel()) == Math.signum(futureDif);
		if (Math.abs(angleDif) < AngleMath.deg2rad(targetReachedArmEarlyOverstepCheck) && overstepped)
		{
			return true;
		}

		return angleDif * futureDif < 0;
	}


	private boolean isRoughlyTargeted(double desiredTargetAngle)
	{
		return AngleMath.diffAbs(desiredTargetAngle, getTBot().getOrientation()) < AngleMath.deg2rad(90);
	}
}
