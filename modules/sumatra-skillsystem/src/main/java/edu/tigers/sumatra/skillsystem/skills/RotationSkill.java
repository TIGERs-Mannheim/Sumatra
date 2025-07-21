/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;


import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


/**
 * Perform a simple rotation by some degree at the current location.
 */
public class RotationSkill extends AMoveToSkill
{
	@Setter
	private double rotation;

	private double initialOrientation;

	private final TimestampTimer successTimer = new TimestampTimer(0.2);

	public RotationSkill()
	{

	}

	public RotationSkill(double rotation)
	{
		this.rotation = rotation;
	}

	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		this.initialOrientation = getAngle();

		getMoveCon().setBallObstacle(false);
		getMoveCon().setBotsObstacle(false);
		getMoveCon().setGameStateObstacle(false);
	}

	@Override
	public void doUpdate()
	{
		setTargetPose(getPos(), initialOrientation + rotation, defaultMoveConstraints());
		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));

		successTimer.update(getWorldFrame().getTimestamp());
		if (successTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			setSkillState(ESkillState.SUCCESS);
		}
	}
}
