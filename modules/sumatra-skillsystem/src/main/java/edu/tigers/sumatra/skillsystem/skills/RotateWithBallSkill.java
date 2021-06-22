/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
public class RotateWithBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Dribbler speed while protecting", defValue = "5000.0")
	private static double protectDribbleSpeed = 5000;

	@Configurable(defValue = "3.0")
	private static double accMax = 3.0;

	@Configurable(defValue = "1.5")
	private static double velMax = 1.5;

	@Configurable(defValue = "2.0")
	private static double velMaxW = 2.0;

	@Setter
	IVector2 protectionTarget;

	private TimestampTimer changeStateTimer = new TimestampTimer(0.1);


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		getMoveConstraints().setAccMax(accMax);
		getMoveConstraints().setVelMax(velMax);
		getMoveConstraints().setVelMaxW(velMaxW);

		double targetOrientation = protectionTarget.subtractNew(getPos()).multiplyNew(-1).getAngle();
		updateTargetAngle(targetOrientation);

		if (AngleMath.diffAbs(targetOrientation, getAngle()) < 0.1)
		{
			setSkillState(ESkillState.SUCCESS);
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
