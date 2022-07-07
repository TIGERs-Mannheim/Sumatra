/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
public class RotateWithBallSkill extends AMoveToSkill
{
	@Configurable(defValue = "4.0")
	private static double accMax = 4.0;

	@Configurable(defValue = "2.5")
	private static double velMax = 2.5;

	@Configurable(defValue = "10.0")
	private static double velMaxW = 10.0;

	@Setter
	IVector2 protectionTarget;

	private TimestampTimer changeStateTimer = new TimestampTimer(0.1);

	private IVector2 startPos;



	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setBotsObstacle(false);
		startPos = getPos();
	}


	@Override
	public void doUpdate()
	{
		getMoveConstraints().setAccMax(accMax);
		getMoveConstraints().setVelMax(velMax);
		getMoveConstraints().setVelMaxW(velMaxW);

		double targetOrientation = protectionTarget.subtractNew(getPos()).multiplyNew(-1).getAngle();
		updateTargetAngle(targetOrientation);
		updateDestination(startPos);

		if (AngleMath.diffAbs(targetOrientation, getAngle()) < 0.1)
		{
			setSkillState(ESkillState.SUCCESS);
		}

		if (!getTBot().getBallContact().hasContactFromVisionOrBarrier())
		{
			changeStateTimer.update(getWorldFrame().getTimestamp());
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
}
