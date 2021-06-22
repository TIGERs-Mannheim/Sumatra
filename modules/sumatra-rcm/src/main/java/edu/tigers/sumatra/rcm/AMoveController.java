/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.proto.BotActionCommandProtos;


public abstract class AMoveController implements IMoveController
{
	private static final double TRIGGER_THRESHOLD = 0.001;

	@Configurable(defValue = "4.0")
	private static double rotateDef = 4;

	@Configurable(defValue = "1.0")
	private static double rotateMin = 1;

	@Configurable(defValue = "10.0")
	private static double rotateMax = 10;

	static
	{
		ConfigRegistration.registerClass("rcm", AMoveController.class);
	}

	protected ControllerState controllerState;


	protected double getRotation(final BotActionCommandProtos.BotActionCommand command)
	{
		double rotate = command.getRotate();

		if ((rotate < TRIGGER_THRESHOLD) && (rotate > -TRIGGER_THRESHOLD))
		{
			rotate = 0;
		}

		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		rotate = subtractCompassThreshold(rotate);

		// scale back to full range (-1.0 - 1.0)
		rotate *= 1.0 / (1.0f - TRIGGER_THRESHOLD);

		double rotateSpeed = (rotateDef + ((rotateMax - rotateDef) * command.getAccelerate()))
				- ((rotateDef - rotateMin) * command.getDecelerate());

		rotate = Math.signum(rotate) * rotate * rotate * rotateSpeed;
		return rotate;
	}


	protected double subtractCompassThreshold(final double rotateOrig)
	{
		double rotate = rotateOrig;
		if (rotate < 0)
		{
			rotate += TRIGGER_THRESHOLD;
		}

		if (rotate > 0)
		{
			rotate -= TRIGGER_THRESHOLD;
		}
		return rotate;
	}


	protected void respectControllerDeadZone(final Vector2 setVel)
	{
		if ((setVel.y() < controllerState.getCompassThreshold()) && (setVel.y() > -controllerState.getCompassThreshold()))
		{
			setVel.setY(0);
		}

		if ((setVel.x() < controllerState.getCompassThreshold()) && (setVel.x() > -controllerState.getCompassThreshold()))
		{
			setVel.setX(0);
		}
	}


	protected void subtractCompassThreshold(final Vector2 setVel)
	{
		if (setVel.y() < 0)
		{
			setVel.setY(setVel.y() + controllerState.getCompassThreshold());
		}

		if (setVel.y() > 0)
		{
			setVel.setY(setVel.y() - controllerState.getCompassThreshold());
		}

		if (setVel.x() < 0)
		{
			setVel.setX(setVel.x() + controllerState.getCompassThreshold());
		}

		if (setVel.x() > 0)
		{
			setVel.setX(setVel.x() - controllerState.getCompassThreshold());
		}
	}
}
