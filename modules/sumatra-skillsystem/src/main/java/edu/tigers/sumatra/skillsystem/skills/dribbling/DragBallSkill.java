/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.dribbling;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
public class DragBallSkill extends AMoveToSkill
{
	@Configurable(defValue = "1.2")
	private static double accMax = 1.2;

	@Configurable(defValue = "1.0")
	private static double initialAccMax = 1.0;

	@Configurable(defValue = "5.0")
	private static double accMaxW = 5.0;

	@Configurable(defValue = "1.2")
	private static double velMax = 1.2;

	@Configurable(defValue = "5.0")
	private static double velMaxW = 5.0;

	@Setter
	IVector2 destination;

	@Setter
	double targetOrientation;

	private TimestampTimer changeStateTimer = new TimestampTimer(0.10);


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		getMoveConstraints().setAccMaxW(accMaxW);
		getMoveConstraints().setVelMax(velMax);
		getMoveConstraints().setVelMaxW(velMaxW);

		if (getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
		{
			getMoveConstraints().setAccMax(accMax);
			if (destination != null)
			{
				updateDestination(destination);
			}
			updateTargetAngle(targetOrientation);
		} else
		{
			// no Destination set. AMoveToSkill initializes with getPos() as destination.
			getMoveConstraints().setAccMax(initialAccMax);
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

		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
		super.doUpdate();
	}
}
