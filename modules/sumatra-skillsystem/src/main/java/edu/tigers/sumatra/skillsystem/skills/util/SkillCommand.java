/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A skill command defines zero to n actions that should be executed at a certain time
 */
public class SkillCommand
{
	private final double time;
	private IVector2 xyVel = null;
	private Double aVel = null;
	private Integer dribbleSpeed = null;
	private Double kickSpeed = null;
	private EKickerDevice kickerDevice = null;
	
	
	public SkillCommand(final double time)
	{
		this.time = time;
	}
	
	
	public double getTime()
	{
		return time;
	}
	
	
	public IVector2 getXyVel()
	{
		return xyVel;
	}
	
	
	public void setXyVel(final IVector2 xyVel)
	{
		this.xyVel = xyVel;
	}
	
	
	public Double getaVel()
	{
		return aVel;
	}
	
	
	public void setaVel(final Double aVel)
	{
		this.aVel = aVel;
	}
	
	
	public Integer getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	public void setDribbleSpeed(final Integer dribbleSpeed)
	{
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	public Double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	public void setKickSpeed(final Double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	public EKickerDevice getKickerDevice()
	{
		return kickerDevice;
	}
	
	
	public void setKickerDevice(final EKickerDevice kickerDevice)
	{
		this.kickerDevice = kickerDevice;
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("time", time)
				.append("xyVel", xyVel)
				.append("aVel", aVel)
				.append("dribbleSpeed", dribbleSpeed)
				.append("kickSpeed", kickSpeed)
				.append("kickerDevice", kickerDevice)
				.toString();
	}
}
