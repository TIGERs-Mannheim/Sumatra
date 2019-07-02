/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
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
	private Double accMaxXY = null;
	private Double accMaxW = null;
	
	
	public SkillCommand(final double time)
	{
		this.time = time;
	}
	
	
	public static SkillCommand command(final double time)
	{
		return new SkillCommand(time);
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
	
	
	public Double getAccMaxXY()
	{
		return accMaxXY;
	}
	
	
	public void setAccMaxXY(final Double accMaxXY)
	{
		this.accMaxXY = accMaxXY;
	}
	
	
	public Double getAccMaxW()
	{
		return accMaxW;
	}
	
	
	public void setAccMaxW(final Double accMaxW)
	{
		this.accMaxW = accMaxW;
	}
	
	
	public SkillCommand withXyVel(final IVector2 xyVel)
	{
		this.xyVel = xyVel;
		return this;
	}
	
	
	public SkillCommand withAVel(final Double aVel)
	{
		this.aVel = aVel;
		return this;
	}
	
	
	public SkillCommand withDribbleSpeed(final Integer dribbleSpeed)
	{
		this.dribbleSpeed = dribbleSpeed;
		return this;
	}
	
	
	public SkillCommand withKickSpeed(final Double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
		return this;
	}
	
	
	public SkillCommand withKickerDevice(final EKickerDevice kickerDevice)
	{
		this.kickerDevice = kickerDevice;
		return this;
	}
	
	
	public SkillCommand withAccMaxXY(final Double accMaxXY)
	{
		this.accMaxXY = accMaxXY;
		return this;
	}
	
	
	public SkillCommand withAccMaxW(final Double accMaxW)
	{
		this.accMaxW = accMaxW;
		return this;
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("time", time)
				.append("xyVel", xyVel)
				.append("aVel", aVel)
				.append("dribbleSpeed", dribbleSpeed)
				.append("kickSpeed", kickSpeed)
				.append("kickerDevice", kickerDevice)
				.toString();
	}
}
