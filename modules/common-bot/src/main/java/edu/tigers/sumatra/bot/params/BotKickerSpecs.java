/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.bot.params;

import com.sleepycat.persist.model.Persistent;


/**
 * Robot kicker specifications.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class BotKickerSpecs implements IBotKickerSpecs
{
	private double	chipAngle					= 45.0;
	private double	maxAbsoluteChipVelocity	= 8.0;
	
	
	/**
	 * @return the chipAngle
	 */
	@Override
	public double getChipAngle()
	{
		return chipAngle;
	}
	
	
	/**
	 * @param chipAngle the chipAngle to set
	 */
	public void setChipAngle(final double chipAngle)
	{
		this.chipAngle = chipAngle;
	}
	
	
	/**
	 * @return the maxAbsoluteChipVelocity
	 */
	@Override
	public double getMaxAbsoluteChipVelocity()
	{
		return maxAbsoluteChipVelocity;
	}
	
	
	/**
	 * @param maxAbsoluteChipVelocity the maxAbsoluteChipVelocity to set
	 */
	public void setMaxAbsoluteChipVelocity(final double maxAbsoluteChipVelocity)
	{
		this.maxAbsoluteChipVelocity = maxAbsoluteChipVelocity;
	}
}
