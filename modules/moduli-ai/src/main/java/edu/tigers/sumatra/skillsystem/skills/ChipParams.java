/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

/**
 * Data holder for chip parameters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipParams
{
	private final double	kickSpeed;
	private final int		dribbleSpeed;
	
	
	/**
	 * @param kickSpeed
	 * @param dribbleSpeed
	 */
	public ChipParams(final double kickSpeed, final int dribbleSpeed)
	{
		this.kickSpeed = kickSpeed;
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	/**
	 * @return the kickSpeed
	 */
	public final double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @return the dribbleSpeed
	 */
	public final int getDribbleSpeed()
	{
		return dribbleSpeed;
	}
}
