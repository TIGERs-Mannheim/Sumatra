/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.09.2016
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

/**
 * @author AndreR
 */
public enum EDataAcquisitionMode
{
	/**  */
	NONE(0),
	
	/**  */
	MOTOR_MODEL(1),
	
	/**  */
	BOT_MODEL(2),
	
	/**  */
	DELAYS(3);
	
	private final int id;
	
	
	/**
	 */
	private EDataAcquisitionMode(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static EDataAcquisitionMode getModeConstant(final int id)
	{
		for (EDataAcquisitionMode s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}
		
		return NONE;
	}
	
	
	/**
	 * @return
	 */
	public int getId()
	{
		return id;
	}
}
