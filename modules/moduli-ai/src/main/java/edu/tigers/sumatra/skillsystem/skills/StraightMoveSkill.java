/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;


/**
 * Simple straight move that times out after a specified time.
 * Mainly for testing purposes.
 * 
 * @author AndreR
 */
public class StraightMoveSkill extends PositionSkill
{
	/** mm */
	private final int		distance;
	/** rad */
	private final double	angle;
								
								
	/**
	 * @param distance [mm]
	 * @param angle [rad]
	 */
	public StraightMoveSkill(final int distance, final double angle)
	{
		super(ESkill.STRAIGHT_MOVE);
		
		this.distance = distance;
		this.angle = angle;
	}
	
	
	@Override
	protected void onSkillStarted()
	{
		setDestination(getPos().addNew(new Vector2(getAngle() + angle).multiply(distance)));
	}
	
	
}
