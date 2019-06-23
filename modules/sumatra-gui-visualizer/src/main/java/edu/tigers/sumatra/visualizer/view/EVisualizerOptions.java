/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.view;

import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;


/**
 * Available visualizer options and their names
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated string literals
public enum EVisualizerOptions implements IShapeLayer
{
	/**  */
	FANCY("Visualizer", "fancy drawing"),
	/**  */
	TURN_NEXT("Visualizer", "horizontal field"),
	/**  */
	RESET_FIELD("Visualizer", "reset field"),
	/**  */
	PAINT_COORD("Visualizer", "paint coord."),
	/**  */
	YELLOW_AI_PRIMARY("Visualizer", "Yellow AI Primary"),
	/**  */
	BLUE_AI_PRIMARY("Visualizer", "Blue AI Primary"),
	/**  */
	YELLOW_AI_SECONDARY("Visualizer", "Yellow AI Secondary"),
	/**  */
	BLUE_AI_SECONDARY("Visualizer", "Blue AI Secondary"),;
	
	
	private final String name;
	private final String category;
	
	
	/**
	 * @param name this name will be shown to the user
	 */
	EVisualizerOptions(final String category, final String name)
	{
		this.name = name;
		this.category = category;
	}
	
	
	/**
	 * @return the name
	 */
	@Override
	public final String getLayerName()
	{
		return name;
	}
	
	
	@Override
	public String getCategory()
	{
		return category;
	}
	
	
	@Override
	public String getId()
	{
		return name();
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return true;
	}
}
