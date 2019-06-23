/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 22, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EDrawableShapesLayer implements IVisualizerOption
{
	/**  */
	INJECT("Inject"),
	/**  */
	UNSORTED("Unsorted"),
	/** */
	DEFENSE("Defense"),
	/** */
	DEFENSE_ADDITIONAL("Defense - additional"),
	/**  */
	TOP_GPU_GRID("Top GPU grid pos"),
	/**  */
	OFFENSIVE("Offensive"),
	/**  */
	GOAL_POINTS("Goal points"),
	/**  */
	BALL_POSSESSION("Ball possession"),
	/** */
	BIG_DATA("Goal chance heatmap"),
	/** */
	LEARNING("Learning"),
	/** */
	Keeper("Keeper");
	
	
	private final String name;
	
	
	private EDrawableShapesLayer(final String name)
	{
		this.name = name;
	}
	
	
	@Override
	public String getName()
	{
		return name;
	}
	
	
}
