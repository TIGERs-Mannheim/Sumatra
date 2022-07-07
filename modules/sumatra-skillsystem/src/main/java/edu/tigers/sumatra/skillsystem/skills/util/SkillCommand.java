/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Data;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * A skill command defines zero to n actions that should be executed at a certain time
 */
@Data
public class SkillCommand
{
	private final double time;
	private IVector2 xyVel = null;
	private Double aVel = null;
	private Integer dribbleSpeed = null;
	private Double dribbleCurrent = null;
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
