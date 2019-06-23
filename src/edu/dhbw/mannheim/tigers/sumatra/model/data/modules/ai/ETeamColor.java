/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.09.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.awt.Color;


/**
 * Team colors.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public enum ETeamColor
{
	/** */
	BLUE(1),
	/** */
	YELLOW(0),
	/**  */
	@Deprecated
	BOTH(3),
	/**  */
	@Deprecated
	NO(4),
	/** */
	UNINITIALIZED(2);
	
	private final int	id;
	
	
	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
	
	
	private ETeamColor(int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static ETeamColor opposite(ETeamColor color)
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
	
	
	/**
	 * @return
	 */
	public ETeamColor opposite()
	{
		return opposite(this);
	}
	
	
	/**
	 * @return
	 */
	public Color getColor()
	{
		switch (this)
		{
			case BLUE:
				return Color.BLUE;
			case YELLOW:
				return Color.YELLOW.darker().darker();
			case UNINITIALIZED:
				return Color.black;
			default:
				break;
		}
		throw new IllegalStateException();
	}
}
