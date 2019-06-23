/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 23, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.skillsystem.ESkill;


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
