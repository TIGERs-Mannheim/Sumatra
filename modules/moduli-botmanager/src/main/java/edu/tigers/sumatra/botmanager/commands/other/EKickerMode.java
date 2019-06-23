/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 11, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.other;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EKickerMode
{
	/**  */
	DISARM(0),
	/**  */
	ARM(1),
	/**  */
	FORCE(2),
	/** */
	ARM_TIME(3),
	/** */
	ARM_AIM(4),
	/**  */
	NONE(0x0F);
	private final int id;
	
	
	private EKickerMode(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
}
