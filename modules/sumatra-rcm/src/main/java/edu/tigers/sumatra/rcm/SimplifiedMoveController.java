/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.proto.BotActionCommandProtos;


public class SimplifiedMoveController extends AMoveController
{
	@Configurable(defValue = "3.0")
	private static double accDef = 3;
	@Configurable(defValue = "4.0")
	private static double accMax = 4;

	@Configurable(defValue = "4.0")
	private static double dccDef = 4;
	@Configurable(defValue = "10.0")
	private static double dccMax = 10;

	@Configurable(defValue = "1.5")
	private static double speedMaxLow = 1.5;
	@Configurable(defValue = "3.5")
	private static double speedMaxHigh = 3.5;

	static
	{
		ConfigRegistration.registerClass("rcm", SimplifiedMoveController.class);
	}

	private IVector2 lastVel = Vector2.fromXY(0, 0);
	private long lastTimestamp = System.nanoTime();


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

		setVel.multiply(speedMax);

		final double acc = getAcc(command, setVel);

		lastVel = setVel;

		double rotate = getRotation(command);

		return moveWithLocalVel(acc, setVel, rotate);
	}


	private double getAcc(final BotActionCommandProtos.BotActionCommand command, final Vector2 setVel)
	{
		if (!setVel.isZeroVector() && (lastVel.isZeroVector()
				|| (setVel.angleToAbs(lastVel).orElse(0.0) < AngleMath.PI_HALF)))
		{
			// acc
			return accDef + ((accMax - accDef) * command.getAccelerate());
		}
		// dcc
		return dccDef + ((dccMax - dccDef) * command.getDecelerate());
	}


	private AMoveBotSkill moveWithLocalVel(final double acc, final IVector2 outVel, final double rotate)
	{
		final BotSkillLocalVelocity skill = new BotSkillLocalVelocity(outVel, rotate,
				new MoveConstraints(controllerState.getBot().getBotParams().getMovementLimits()));
		skill.setAccMax(acc);
		return skill;
	}
}
