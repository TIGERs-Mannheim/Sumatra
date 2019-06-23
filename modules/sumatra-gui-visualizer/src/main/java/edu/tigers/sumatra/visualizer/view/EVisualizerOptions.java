/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;


/**
 * Available visualizer options and their names
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
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
	YELLOW_AI("Visualizer", "Yellow AI"),
	/**  */
	BLUE_AI("Visualizer", "Blue AI"),;
	
	
	private final String	name;
	private final String	category;
								
								
	/**
	 * @param name this name will be shown to the user
	 */
	private EVisualizerOptions(final String category, final String name)
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
