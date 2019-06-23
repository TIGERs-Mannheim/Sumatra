/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.PositionDriver;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;


/**
 * Move to a given destination and orientation with PositionController
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PositionSkill extends AMoveSkill
{
	private final PositionDriver	positionDriver	= new PositionDriver();
	
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param dest
	 * @param orient
	 */
	public PositionSkill(final IVector2 dest, final double orient)
	{
		this(ESkill.POSITION, dest, orient);
	}
	
	
	/**
	 * Use this if you extend from this skill
	 * 
	 * @param skillName
	 */
	protected PositionSkill(final ESkill skillName)
	{
		this(skillName, null, 0);
	}
	
	
	/**
	 * Use this if you extend from this skill
	 * 
	 * @param skillName
	 * @param dest
	 * @param orient
	 */
	protected PositionSkill(final ESkill skillName, final IVector2 dest, final double orient)
	{
		super(skillName);
		setPathDriver(positionDriver);
		positionDriver.setDestination(dest);
		positionDriver.setOrientation(orient);
	}
	
	
	/**
	 * @param destination the destination to set
	 */
	public final void setDestination(final IVector2 destination)
	{
		positionDriver.setDestination(destination);
	}
	
	
	/**
	 * @param orientation the orientation to set
	 */
	public final void setOrientation(final double orientation)
	{
		positionDriver.setOrientation(orientation);
	}
}
