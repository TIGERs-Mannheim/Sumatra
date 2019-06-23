/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PositionSkill extends AMoveToSkill
{
	/**
	 */
	public PositionSkill()
	{
		super(ESkill.POSITION);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		
		setTargetPose(getMoveCon().getDestination(), getMoveCon().getTargetAngle());
	}
}
