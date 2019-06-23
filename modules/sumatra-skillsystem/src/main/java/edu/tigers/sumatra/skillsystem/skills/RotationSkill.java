/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.AState;


/**
 * Perform a simple rotation by some degree at the current location.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RotationSkill extends AMoveSkill
{
	private final double rotation;
	
	
	/**
	 * @param rotation the rotation to perform (can be negative)
	 */
	public RotationSkill(final double rotation)
	{
		super(ESkill.ROTATION);
		this.rotation = rotation;
		setInitialState(new DefaultState());
	}
	
	private class DefaultState extends AState
	{
		double targetAngle;
		
		
		@Override
		public void doEntryActions()
		{
			IVector2 dest = getPos();
			targetAngle = getAngle() + rotation;
			setTargetPose(dest, targetAngle);
		}
	}
}
