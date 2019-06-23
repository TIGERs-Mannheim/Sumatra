/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices;

/**
 * Data holder for chip parameters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ChipParams
{
	private final int	duration;
	private final int	dribbleSpeed;
	
	
	/**
	 * @param duration
	 * @param dribbleSpeed
	 */
	public ChipParams(int duration, int dribbleSpeed)
	{
		this.duration = duration;
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	/**
	 * @return the duration
	 */
	public final int getDuration()
	{
		return duration;
	}
	
	
	/**
	 * @return the dribbleSpeed
	 */
	public final int getDribbleSpeed()
	{
		return dribbleSpeed;
	}
}
