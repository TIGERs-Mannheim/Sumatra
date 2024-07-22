/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
@NoArgsConstructor
public class ProtectiveGetBallSkill extends ABallHandlingSkill
{
	@Configurable(defValue = "1.2")
	private static double ballContactAccMax = 1.2;

	@Configurable(defValue = "5.0")
	private static double ballContactAccMaxW = 5.0;

	@Configurable(defValue = "5.0")
	private static double velMaxW = 5.0;

	@Setter
	private boolean strongDribblerContactNeeded = false;


	private boolean succeedEarly = false;
	private TimestampTimer successTimer = new TimestampTimer(0.15);


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		succeedEarly = isSucceeded();
		setStopOnContact(true);
	}


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm().withDribblerMode(calcDribbleMode()));

		getMoveCon().setBotsObstacle(
				getBall().getPos().distanceTo(getTBot().getBotKickerPos()) - Geometry.getBotRadius() > 50);

		setSkillState(calcSkillState());

		if (this.isStopped())
		{
			getMoveConstraints().setAccMax(ballContactAccMax);
			getMoveConstraints().setAccMaxW(ballContactAccMaxW);
		} else
		{
			getMoveConstraints().resetLimits(getBot().getBotParams().getMovementLimits());
		}
		getMoveConstraints().setVelMaxW(velMaxW);

		super.doUpdate();
	}


	private boolean isSucceeded()
	{
		if (!getTBot().getRobotInfo().isBarrierInterrupted())
		{
			return false;
		}

		return !strongDribblerContactNeeded
				|| getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG;
	}


	private ESkillState calcSkillState()
	{
		if (succeedEarly)
		{
			return ESkillState.SUCCESS;
		}

		if (isSucceeded())
		{
			successTimer.update(getWorldFrame().getTimestamp());
		} else
		{
			successTimer.reset();
		}

		if (successTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			return ESkillState.SUCCESS;
		} else
		{
			return ESkillState.IN_PROGRESS;
		}
	}


	private EDribblerMode calcDribbleMode()
	{
		if (getPos().distanceTo(getBall().getPos()) < 300)
		{
			if (strongDribblerContactNeeded)
			{
				return EDribblerMode.HIGH_POWER;
			} else
			{
				return EDribblerMode.DEFAULT;
			}
		}
		return EDribblerMode.OFF;
	}
}

