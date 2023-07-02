/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;


public class ChopTrickState extends AAttackerRoleState<MoveToSkill>
{
	@Configurable(defValue = "0.1", comment = "Delay to wait ")
	private static double startTurnDelay = 0.1;

	@Configurable(defValue = "0.4", comment = "Delay to end state")
	private static double endStateTime = 0.4;

	private final TimestampTimer startTurnDelayTimer = new TimestampTimer(startTurnDelay);

	private final TimestampTimer endStateTimer = new TimestampTimer(endStateTime);

	private boolean initialRun = false;


	public ChopTrickState(AttackerRole role)
	{
		super(MoveToSkill::new, role, EAttackerState.CHOP_TRICK);
	}


	@Override
	protected void onInit()
	{
		super.onInit();
		endStateTimer.reset();
		startTurnDelayTimer.reset();
		initialRun = true;
	}


	@Override
	protected void doStandardUpdate()
	{
		IVector2 target = Geometry.getGoalTheir().getCenter();
		double angle = 0;
		if (target != null)
		{
			IVector2 ballToTarget = target.subtractNew(getRole().getWFrame().getBall().getPos());
			IVector2 meToBall = getRole().getWFrame().getBall().getPos().subtractNew(getRole().getPos());
			angle = meToBall.angleTo(ballToTarget).orElse(0.0);
		}
		double rotationAngle;
		if (angle > 0)
		{
			// right
			rotationAngle = AngleMath.deg2rad(170);
		} else
		{
			// left
			rotationAngle = AngleMath.deg2rad(-170);
		}
		skill.updateDestination(getRole().getPos());

		skill.getMoveCon().setTheirBotsObstacle(false);
		skill.getMoveCon().setOurBotsObstacle(false);
		skill.getMoveCon().setBallObstacle(false);
		skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));

		startTurnDelayTimer.update(getRole().getWFrame().getTimestamp());
		if (startTurnDelayTimer.isTimeUp(getRole().getWFrame().getTimestamp()) && initialRun)
		{
			initialRun = false;
			skill.updateTargetAngle(getRole().getBot().getOrientation() + rotationAngle);
		}

		endStateTimer.update(getRole().getWFrame().getTimestamp());
		if (endStateTimer.isTimeUp(getRole().getWFrame().getTimestamp()))
		{
			getRole().triggerEvent(EAttackerState.PROTECT);
		}
	}
}

