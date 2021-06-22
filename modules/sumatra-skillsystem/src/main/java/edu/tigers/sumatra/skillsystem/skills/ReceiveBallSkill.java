/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class ReceiveBallSkill extends ABallArrivalSkill
{
	@Configurable(comment = "Dribble speed during receive", defValue = "3000.0")
	private static double dribbleSpeed = 3000;


	private final TimestampTimer receiveDelayTimer = new TimestampTimer(0.1);


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm()
				.withDribbleSpeed(getTBot().getBallContact().getContactDuration() > 0.1 ? 0 : dribbleSpeed));
		setDesiredTargetAngle(calcTargetAngle());
		super.doUpdate();
		setSkillState(calcSkillState());
	}


	private ESkillState calcSkillState()
	{
		if (getTBot().getBallContact().isBallContactFromVision()
				|| getTBot().getBallContact().hadContact(0.1)
				|| getBall().getPos().distanceTo(getPos()) < Geometry.getBotRadius())
		{
			receiveDelayTimer.update(getWorldFrame().getTimestamp());
			if (receiveDelayTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				return ESkillState.SUCCESS;
			}
		} else
		{
			receiveDelayTimer.reset();
			if (!ballIsMoving() || !getBall().getTrajectory().getTravelLine().isPointInFront(getPos()))
			{
				return ESkillState.FAILURE;
			}
		}
		return ESkillState.IN_PROGRESS;
	}


	private double calcTargetAngle()
	{
		if (getBall().getVel().getLength2() > 0.5)
		{
			return getBall().getVel().getAngle() + AngleMath.DEG_180_IN_RAD;
		}
		return getAngle();
	}
}
