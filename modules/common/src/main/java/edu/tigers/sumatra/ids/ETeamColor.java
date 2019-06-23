/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.09.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.export.INumberListable;


/**
 * Team colors.
 * 
 * @author Oliver Steinbrecher
 */
public enum ETeamColor implements INumberListable
{
	/** */
	YELLOW(0),
	/** */
	BLUE(1),
	/**  */
	NEUTRAL(2),
	/** */
	UNINITIALIZED(3);
	
	private final int	id;
	
	
	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
	
	
	private ETeamColor(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static ETeamColor opposite(final ETeamColor color)
	{
		switch (color)
		{
			case YELLOW:
				return BLUE;
				
			case BLUE:
				return YELLOW;
				
			case NEUTRAL:
				return NEUTRAL;
				
			default:
				return UNINITIALIZED;
		}
	}
	
	
	/**
	 * @return
	 */
	public static ETeamColor[] yellowBlueValues()
	{
		return new ETeamColor[] { YELLOW, BLUE };
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
				return Color.YELLOW;
			case UNINITIALIZED:
				return Color.black;
			default:
				break;
		}
		throw new IllegalStateException();
	}
	
	
	/**
	 * Returns true if {@code color} is set to either {@code BLUE} or {@code YELLOW}
	 * 
	 * @param color
	 * @return true if the parameter is set to a color
	 */
	public static boolean isNonNeutral(final ETeamColor color)
	{
		return (color != NEUTRAL) && (color != UNINITIALIZED);
	}
	
	
	/**
	 * Returns true if {@code color} is set to either {@code BLUE} or {@code YELLOW}
	 * 
	 * @return true if the parameter is set to a color
	 */
	public boolean isNonNeutral()
	{
		return isNonNeutral(this);
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(this == ETeamColor.BLUE ? 1 : this == ETeamColor.YELLOW ? 0 : -1);
		return numbers;
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public static ETeamColor fromNumberList(final Number value)
	{
		return value.intValue() == 0 ? ETeamColor.YELLOW : value.intValue() == 1 ? ETeamColor.BLUE
				: ETeamColor.UNINITIALIZED;
	}
}
