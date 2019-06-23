/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

/**
 * Available visualizer options and their names
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EVisualizerOptions
{
	/**  */
	LAYER_DEBUG_INFOS("layer DEBUG infos"),
	/**  */
	FANCY("fancy drawing"),
	/**  */
	SHAPES("shapes"),
	/**  */
	COORDINATES("coordinates"),
	/**  */
	TURN_NEXT("horizontal field"),
	/**  */
	RESET_FIELD("reset field"),
	/**  */
	POSITION_GRID("position grid"),
	/**  */
	ANALYSIS_GRID("analyze grid"),
	/**  */
	VELOCITY("velocity"),
	/**  */
	ACCELERATION("acceleration"),
	/**  */
	PATHS("paths"),
	/**  */
	SPLINES("splines"),
	/** */
	ERROR_TREE("error tree"),
	/**  */
	PATTERNS("patterns"),
	/**  */
	GOAL_POINTS("goal points"),
	/**  */
	FIELD_MARKS("field marks"),
	/**  */
	ROLE_NAME("rolenames"),
	/**  */
	TACTICS("tactics"),
	/**  */
	BALL_BUFFER("ball buffer"),
	/**  */
	YELLOW_AI("Yellow AI"),
	/**  */
	BLUE_AI("Blue AI"),
	/**  */
	BOT_STATUS("Bot Status"),
	/**  */
	REFEREE("Referee"),
	/**  */
	FIELD_PREDICTION("Field prediction"),
	/**  */
	POT_PATHS("Potential paths"),
	/**  */
	POT_SPLINES("Potential Splines"),
	/**  */
	SUPPORT_POS("Support Positions"),
	/**  */
	MISC("Misc");
	
	
	private final String	name;
	
	
	/**
	 * @param name this name will be shown to the user
	 */
	private EVisualizerOptions(final String name)
	{
		this.name = name;
	}
	
	
	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}
}
