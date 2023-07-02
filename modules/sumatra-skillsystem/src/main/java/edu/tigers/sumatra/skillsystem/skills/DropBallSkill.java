/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;


/**
 * Drop the ball smoothly by reducing the max dribble current.
 */
public class DropBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Dribble current reduction rate [A/s]", defValue = "4.0")
	private static double dribbleCurrentReductionRate = 4.0;

	@Configurable(comment = "Min dribble current [A] ", defValue = "1.0")
	private static double dribbleCurrentMin = 1.0;

	@Configurable(comment = "Move back during drop.", defValue = "true")
	private static boolean moveBack = true;

	@Configurable(comment = "Velocity for move back.", defValue = "0.1")
	private static double moveBackVelocity = 0.1;

	private double maxDribbleCurrent;
	private long tStart;
	private IVector2 moveBackTarget;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		maxDribbleCurrent = getBot().getBotParams().getDribblerSpecs().getDefaultMaxCurrent();
		tStart = getWorldFrame().getTimestamp();
		moveBackTarget = Vector2.fromAngleLength(getAngle(), -50).add(getPos());
	}


	@Override
	public void doUpdate()
	{
		super.doUpdate();

		if (!getTBot().getBallContact().hadContact(0.2))
		{
			setSkillState(ESkillState.FAILURE);
			return;
		}

		double timePast = (getWorldFrame().getTimestamp() - tStart) / 1e9;
		double defDribbleCurrent = getBot().getBotParams().getDribblerSpecs().getDefaultMaxCurrent();
		double scaledDribbleCurrent = defDribbleCurrent - timePast * dribbleCurrentReductionRate;
		if (moveBack && scaledDribbleCurrent < (defDribbleCurrent + dribbleCurrentMin) / 2)
		{
			updateDestination(moveBackTarget);
			getMoveConstraints().setVelMax(moveBackVelocity);
		}
		if (scaledDribbleCurrent < dribbleCurrentMin)
		{
			maxDribbleCurrent = dribbleCurrentMin;
			setSkillState(ESkillState.SUCCESS);
		} else
		{
			maxDribbleCurrent = scaledDribbleCurrent;
			setSkillState(ESkillState.IN_PROGRESS);
		}

		setKickParams(KickParams.disarm().withDribbleSpeedRpm(
				getBot().getBotParams().getDribblerSpecs().getDefaultSpeed(),
				maxDribbleCurrent
		));
	}
}
