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
public abstract class AMoveToSkill extends AMoveSkill
{
	
	/**
	 * 
	 */
	protected AMoveToSkill(final ESkill skill)
	{
		super(skill);
	}
	
	
	/**
	 * Create the configured default MoveToSkill
	 * 
	 * @return
	 */
	public static AMoveToSkill createMoveToSkill()
	{
		return new MoveToTrajSkill();
	}
	
}
