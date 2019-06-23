/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.IVisualizerOption;


/**
 * Available visualizer options and their names
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EVisualizerOptions implements IVisualizerOption
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
	VELOCITY("velocity"),
	/**  */
	ACCELERATION("acceleration"),
	/**  */
	PATHS("paths"),
	/**  */
	PATH_DECORATION("path decoration"),
	/** */
	PP_DEBUG("path debug"),
	/**  */
	FIELD_MARKS("field marks"),
	/**  */
	ROLE_NAME("rolenames"),
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
	POT_PATH_DECORATION("Potential path dec."),
	/**  */
	POT_DEBUG("Potential Debug"),
	/**  */
	SUPPORT_POS("Support Positions"),
	/**  */
	SUPPORT_GRID("Support Grid"),
	/***/
	INTERSECTION("Cam intersection"),
	/**  */
	POSITION_BUFFER("Pos Buffer"),
	/**  */
	PATHS_UNSMOOTHED("Unsmoothed paths"),
	/**  */
	PATHS_RAMBO("Rambo tree"),
	/**  */
	VISION("Vision");
	
	
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
	@Override
	public final String getName()
	{
		return name;
	}
}
