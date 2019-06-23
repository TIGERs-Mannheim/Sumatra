/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.AroundBallDriver;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Prepare for redirect
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class DelayedKickSkill extends AMoveSkill
{
	private IVector2						target;
	
	private final AroundBallDriver	driver;
	
	
	/**
	 * @param target
	 */
	public DelayedKickSkill(final IVector2 target)
	{
		super(ESkill.DELAYED_KICK);
		this.target = target;
		driver = new AroundBallDriver(new DynamicPosition(target));
		setPathDriver(driver);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
	}
	
	
	/**
	 * @return the target
	 */
	public final IVector2 getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public final void setTarget(final IVector2 target)
	{
		this.target = target;
	}
	
	
}
