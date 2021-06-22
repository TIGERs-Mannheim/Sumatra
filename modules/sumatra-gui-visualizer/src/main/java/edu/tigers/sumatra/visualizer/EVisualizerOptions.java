/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.drawable.IShapeLayer;


/**
 * Available visualizer options and their names
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated string literals
public enum EVisualizerOptions implements IShapeLayer
{
	/**  */
	FANCY("Visualizer", "Fancy drawing"),
	/**  */
	TURN_NEXT("Visualizer", "Horizontal field"),
	/**  */
	RESET_FIELD("Visualizer", "Reset field"),
	/**  */
	DARK("Visualizer", "Dark mode", false);
	
	
	private final String name;
	private final String category;
	private final boolean isVisibleByDefault;
	
	
	EVisualizerOptions(final String category, final String name)
	{
		this(category, name, true);
	}
	
	
	/**
	 * @param name this name will be shown to the user
	 */
	EVisualizerOptions(final String category, final String name, final boolean isVisibleByDefault)
	{
		this.name = name;
		this.category = category;
		this.isVisibleByDefault = isVisibleByDefault;
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
		return isVisibleByDefault;
	}
}
