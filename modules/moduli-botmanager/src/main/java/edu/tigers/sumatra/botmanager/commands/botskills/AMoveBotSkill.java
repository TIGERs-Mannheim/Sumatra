/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 25, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AMoveBotSkill extends ABotSkill
{
	// /** [m/s] */
	// @SerialData(type = ESerialDataType.UINT16)
	// private int kickSpeed;
	//
	// @SerialData(type = ESerialDataType.UINT8)
	// private int kickFlags;
	//
	// @SerialData(type = ESerialDataType.UINT16)
	// private int dribbleSpeed = 0;
	
	
	/**
	 * @param skill
	 */
	public AMoveBotSkill(final EBotSkill skill)
	{
		super(skill);
	}
	
	
	// /**
	// * Set kick details.
	// *
	// * @param kickSpeed [m/s]
	// * @param device STRAIGHT or CHIP
	// * @param mode FORCE, ARM or DISARM
	// */
	// public void setKick(final double kickSpeed, final EKickerDevice device, final EKickerMode mode)
	// {
	// this.kickSpeed = (int) kickSpeed * 1000;
	// kickFlags &= ~(0xF3); // setAutocharge also modifies a bit in this field
	// kickFlags |= device.getValue() | (mode.getId() << 1);
	// }
	//
	//
	// /**
	// * @param dribbleSpeed the dribbleSpeed to set
	// */
	// public final void setDribbleSpeed(final int dribbleSpeed)
	// {
	// this.dribbleSpeed = dribbleSpeed;
	// }
}
