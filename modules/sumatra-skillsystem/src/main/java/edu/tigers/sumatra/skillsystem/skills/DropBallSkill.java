/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;


/**
 * Drop the ball smoothly by reducing the max dribble current.
 */
public class DropBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Move back during drop.", defValue = "true")
	private static boolean moveBack = true;

	@Configurable(comment = "Velocity for move back.", defValue = "0.05")
	private static double moveBackVelocity = 0.05;

	@Configurable(comment = "Acceleration for move back.", defValue = "5")
	private static double moveBackAcceleration = 5;

	@Configurable(comment = "Time to calm down after dribble traction is OFF", defValue = "0.5")
	private static double calmDownBeforeTime = 0.5;

	@Configurable(comment = "Time to calm down after moving away from ball", defValue = "0.5")
	private static double calmDownAfterTime = 0.5;

	@Configurable(comment = "Distance to move away from ball", defValue = "40")
	private static double moveAwayDistance = 40;

	private IVector2 moveBackTarget;
	private final TimestampTimer calmDownBeforeTimer = new TimestampTimer(calmDownBeforeTime);
	private final TimestampTimer calmDownAfterTimer = new TimestampTimer(calmDownAfterTime);


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		moveBackTarget = Vector2.fromAngleLength(getAngle(), -moveAwayDistance).add(getPos());
	}


	@Override
	public void doUpdate()
	{
		super.doUpdate();

		// We won't move that much, just ignore all obstacles to make sure we correctly drop the ball
		getMoveCon().noObstacles();

		if (!getTBot().getBallContact().hadContact(calmDownAfterTime + 0.1))
		{
			setSkillState(ESkillState.FAILURE);
			return;
		}

		if (getBot().getLastReceivedBotFeedback().getDribbleTraction() == EDribbleTractionState.OFF)
		{
			calmDownBeforeTimer.update(getWorldFrame().getTimestamp());
			if (!calmDownBeforeTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				setSkillState(ESkillState.IN_PROGRESS);
			} else if (moveBack)
			{
				updateDestination(moveBackTarget);
				getMoveConstraints().setVelMax(moveBackVelocity);
				getMoveConstraints().setAccMax(moveBackAcceleration);

				if (getTBot().getPos().distanceTo(moveBackTarget) < 10)
				{
					calmDownAfterTimer.update(getWorldFrame().getTimestamp());
					if (calmDownAfterTimer.isTimeUp(getWorldFrame().getTimestamp()))
					{
						setSkillState(ESkillState.SUCCESS);
					}
				} else {
					calmDownAfterTimer.reset();
				}
			} else
			{
				setSkillState(ESkillState.SUCCESS);
			}
		} else
		{
			calmDownBeforeTimer.reset();
			calmDownAfterTimer.reset();
			setSkillState(ESkillState.IN_PROGRESS);
		}

		setKickParams(KickParams.disarm().withDribbleSpeed(0, 0));
	}
}
