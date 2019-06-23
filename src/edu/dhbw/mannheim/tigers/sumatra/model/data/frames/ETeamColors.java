/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.09.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;


/**
 * Team colors.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public enum ETeamColors
{
	/** */
	BLUE,
	/** */
	YELLOW,
	/** */
	UNINITIALIZED;
	
	/**
	 * @param color
	 * @return
	 */
	public static ETeamColors opposite(ETeamColors color)
	{
		switch (color)
		{
			case YELLOW:
				return BLUE;
				
			case BLUE:
				return YELLOW;
				
			default:
				return UNINITIALIZED;
		}
	}
}
