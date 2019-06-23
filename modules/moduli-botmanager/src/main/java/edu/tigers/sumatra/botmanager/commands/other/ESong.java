/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.07.2016
 * Author(s): ryll
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.other;

/**
 * @author AndreR
 */
public enum ESong
{
	/**  */
	NONE(0),
	/**  */
	CHEERING(1),
	/**  */
	FINAL_COUNTDOWN(2),
	/**  */
	CANTINA_BAND(3);
	private final int id;
	
	
	private ESong(final int id)
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
