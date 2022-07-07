/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class ReceiveBallSkill extends ABallArrivalSkill
{
	@Configurable(defValue = "0.0", comment = "Delay to wait starting from the first barrier interrupted signal")
	private static double receiveDelay = 0.0;

	private final TimestampTimer receiveDelayTimer = new TimestampTimer(receiveDelay);


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm()
				.withDribblerMode(
						getTBot().getBallContact().getContactDuration() > 0.1 ? EDribblerMode.OFF : EDribblerMode.DEFAULT));
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
