/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class ReceiveBallSkill extends ABallArrivalSkill
{
	@Configurable(defValue = "0.1", comment = "Delay to wait starting from the first barrier interrupted signal")
	private static double receiveDelay = 0.1;

	@Configurable(defValue = "1.0", comment = "Delay to wait starting from the first barrier interrupted signal")
	private static double strongDribbelTimeout = 1.0;

	@Configurable(defValue = "OFF")
	private static EDribblerMode dribblerMode = EDribblerMode.OFF;

	@Configurable(defValue = "0", comment = "Distance [mm] between bot and ball when bot should start moving backwards")
	private static double driveBackwardsDist = 0;

	private final TimestampTimer receiveDelayTimer = new TimestampTimer(receiveDelay);
	private final TimestampTimer strongDribbelTimer = new TimestampTimer(strongDribbelTimeout);


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm().withDribblerMode(dribblerMode));
		setDesiredTargetAngle(calcTargetAngle());
		super.doUpdate();
		setSkillState(calcSkillState());
	}


	@Override
	protected IVector2 calcDest()
	{
		IVector2 dest = super.calcDest();
		if (getBall().getVel().getLength2() > 0.3
				&& getBall().getPos().distanceTo(getTBot().getBotKickerPos()) < driveBackwardsDist)
		{
			return dest.subtractNew(Vector2.fromAngleLength(getDesiredTargetAngle(), 250));
		}
		return dest;
	}


	private ESkillState calcSkillState()
	{
		if (getTBot().getBallContact().isBallContactFromVision()
				|| getTBot().getBallContact().hadContact(0.1)
				|| getBall().getPos().distanceTo(getPos()) < Geometry.getBotRadius())
		{
			if (dribblerMode == EDribblerMode.HIGH_POWER)
			{
				return stateForHighPowerDribble();
			} else
			{
				receiveDelayTimer.update(getWorldFrame().getTimestamp());
				if (receiveDelayTimer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					return ESkillState.SUCCESS;
				}
			}
		} else
		{
			receiveDelayTimer.reset();
			strongDribbelTimer.reset();
			if (!ballIsMoving() || !getBall().getTrajectory().getTravelLine().isPointInFront(getPos()))
			{
				return ESkillState.FAILURE;
			}
		}
		return ESkillState.IN_PROGRESS;
	}


	private ESkillState stateForHighPowerDribble()
	{
		if (getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
		{
			return ESkillState.SUCCESS;
		} else
		{
			strongDribbelTimer.update(getWorldFrame().getTimestamp());
			if (strongDribbelTimer.isTimeUp(getWorldFrame().getTimestamp()))
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
