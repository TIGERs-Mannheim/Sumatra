/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;


/**
 * Data holder for chip parameters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickerParams
{
	private EKickerDevice	device			= EKickerDevice.STRAIGHT;
	private EKickerMode		mode				= EKickerMode.NONE;
	private double				speed				= 0;
	private int					dribbleSpeed	= 0;
	
	
	public EKickerDevice getDevice()
	{
		return device;
	}
	
	
	public EKickerMode getMode()
	{
		return mode;
	}
	
	
	public final double getSpeed()
	{
		return speed;
	}
	
	
	public final int getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	public void setDevice(final EKickerDevice device)
	{
		this.device = device;
	}
	
	
	public void setMode(final EKickerMode mode)
	{
		this.mode = mode;
	}
	
	
	public void setSpeed(final double speed)
	{
		this.speed = speed;
	}
	
	
	public void setDribbleSpeed(final int dribbleSpeed)
	{
		this.dribbleSpeed = dribbleSpeed;
	}
}
