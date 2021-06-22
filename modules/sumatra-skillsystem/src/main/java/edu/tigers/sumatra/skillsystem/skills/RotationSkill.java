/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;


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
	}
}
