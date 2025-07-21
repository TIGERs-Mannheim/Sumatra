/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.proto.BotActionCommandProtos;


public class FullMoveController extends AMoveController
{
	@Configurable(comment = "Send smoothed velocities based on acceleration", defValue = "false")
	private static boolean smoothedVel = false;

	@Configurable(defValue = "LOCAL_VEL")
	private static EMovementType moveType = EMovementType.LOCAL_VEL;

	@Configurable(defValue = "3.0")
	private static double accDef = 3;
	@Configurable(defValue = "4.0")
	private static double accMax = 4;

	@Configurable(defValue = "4.0")
	private static double dccDef = 4;
	@Configurable(defValue = "10.0")
	private static double dccMax = 10;

	@Configurable(defValue = "1.0")
	private static double speedDef = 1.0;
	@Configurable(defValue = "0.1")
	private static double speedMin = 0.1;
	@Configurable(defValue = "1.5")
	private static double speedMaxLow = 1.5;
	@Configurable(defValue = "3.5")
	private static double speedMaxHigh = 3.5;

	static
	{
		ConfigRegistration.registerClass("rcm", FullMoveController.class);
	}

	private MatrixMotorModel mm = new MatrixMotorModel();
	private IVector2 lastVel = Vector2.fromXY(0, 0);
	private long lastTimestamp = System.nanoTime();

	private enum EMovementType
	{
		LOCAL_VEL,
		MOTOR_VEL,
	}


	@Override
	public AMoveBotSkill control(final BotActionCommandProtos.BotActionCommand command,
			final ControllerState controllerState)
	{
		this.controllerState = controllerState;
		double speedMax = controllerState.isHighSpeedMode() ? speedMaxHigh : speedMaxLow;

		double dt = (System.nanoTime() - lastTimestamp) / 1e9;
		lastTimestamp = System.nanoTime();

		if (dt > 0.5)
		{
			lastVel = Vector2f.ZERO_VECTOR;
		}

		/*
		 * X-Y-Translation
		 */
		final Vector2 setVel = Vector2.zero();

		setVel.setX(command.getTranslateX());
		setVel.setY(command.getTranslateY());

		// respect controller dead zone
		respectControllerDeadZone(setVel);

		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		subtractCompassThreshold(setVel);

		// scale back to full range (-1.0 - 1.0)
		setVel.multiply(1.0f / (1.0f - controllerState.getCompassThreshold()));

		double speed = (speedDef + ((speedMax - speedDef) * command.getAccelerate()))
				- ((speedDef - speedMin) * command.getDecelerate());
		setVel.multiply(speed);

		final double acc = getAcc(command, setVel);

		IVector2 outVel;
		if (smoothedVel)
		{
			outVel = smoothVelocity(dt, setVel, acc);
		} else
		{
			outVel = setVel;
		}

		lastVel = outVel;

		if (controllerState.getBot().getLastReceivedBotFeedback().getBotFeatures().get(EFeature.V2016) == EFeatureState.WORKING)
		{
			mm.updateGeometry(30, 45, 0.076, 0.025);
		} else
		{
			mm.updateGeometry(45, 45, 0.082, 0.0165);
		}

		switch (moveType)
		{
			case LOCAL_VEL:
				return moveWithLocalVel(acc, outVel, getRotation(command));
			case MOTOR_VEL:
				return moveWithMotorVel(outVel, getRotation(command));
			default:
				throw new IllegalStateException("Invalid move type: " + moveType);
		}
	}


	private double getAcc(final BotActionCommandProtos.BotActionCommand command, final Vector2 setVel)
	{
		final double acc;
		if (!setVel.isZeroVector() &&
				(lastVel.isZeroVector()
						|| (setVel.angleToAbs(lastVel).orElse(0.0) < AngleMath.PI_HALF)))
		{
			// acc
			acc = accDef + ((accMax - accDef) * command.getAccelerate());
		} else
		{
			// dcc
			acc = dccDef + ((dccMax - dccDef) * command.getDecelerate());
		}
		return acc;
	}


	private AMoveBotSkill moveWithMotorVel(final IVector2 outVel, final double rotate)
	{
		IVectorN motors = mm.getWheelSpeed(Vector3.from2d(outVel, rotate));
		return new BotSkillWheelVelocity(motors.toArray());
	}


	private AMoveBotSkill moveWithLocalVel(final double acc, final IVector2 outVel, final double rotate)
	{
		final BotSkillLocalVelocity skill = new BotSkillLocalVelocity(outVel, rotate,
				new MoveConstraints(controllerState.getBot().getBotParams().getMovementLimits()));
		skill.setAccMax(acc);
		return skill;
	}


	private IVector2 smoothVelocity(final double dt, final Vector2 setVel, final double acc)
	{
		final IVector2 outVel;
		IVector2 old2New = setVel.subtractNew(lastVel);
		if (old2New.getLength() > (acc * dt))
		{
			outVel = lastVel.addNew(old2New.scaleToNew(acc * dt));
		} else
		{
			outVel = setVel;
		}
		return outVel;
	}
}
