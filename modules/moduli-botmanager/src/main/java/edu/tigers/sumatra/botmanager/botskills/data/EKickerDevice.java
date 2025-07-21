/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.botskills.data;

/**
 * Choose kicking device
 */
public enum EKickerDevice
{
	STRAIGHT(0),
	CHIP(1);

	private final int value;


	EKickerDevice(final int value)
	{
		this.value = value;
	}


	/**
	 * @return the value
	 */
	public final int getValue()
	{
		return value;
	}


	/**
	 * @param value
	 * @return enum
	 */
	public static EKickerDevice fromValue(final int value)
	{
		for (EKickerDevice s : values())
		{
			if (s.getValue() == value)
			{
				return s;
			}
		}

		return STRAIGHT;
	}
}
