/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 25, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.bot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EFeatureState
{
	/**  */
	WORKING(0),
	/**  */
	KAPUT(2),
	/**  */
	UNKNOWN(0xFF);
	
	private final int	id;
	
	
	private EFeatureState(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	
	
	/**
	 * Convert an id to an enum.
	 * 
	 * @param id
	 * @return enum
	 */
	public static EFeatureState getFeatureStateConstant(final int id)
	{
		for (EFeatureState s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}
		
		return UNKNOWN;
	}
}
