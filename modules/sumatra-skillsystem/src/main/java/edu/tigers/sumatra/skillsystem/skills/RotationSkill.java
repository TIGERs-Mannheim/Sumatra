/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;


import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.RequiredArgsConstructor;


/**
 * Perform a simple rotation by some degree at the current location.
 */
@RequiredArgsConstructor
public class RotationSkill extends AMoveSkill
{
	private final double rotation;


	@Override
	public void doEntryActions()
	{
		setTargetPose(getPos(), getAngle() + rotation, defaultMoveConstraints());
		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
	}
}
