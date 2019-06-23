/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.09.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.INumberListable;


/**
 * Team colors.
 * 
 * @author Oliver Steinbrecher
 */
public enum ETeamColor implements INumberListable
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
