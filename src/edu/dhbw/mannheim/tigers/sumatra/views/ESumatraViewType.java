/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.views;

/**
 * Enum containing all views with their ids and title
 * <p>
 * You should not change the ids, because they are used in the saved layouts
 * <p>
 * Please keep the titles unique, too, as atm they will be used for saving the layout
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ESumatraViewType
{
	/**  */
	AI_CENTER_YELLOW(0, "AI Center Y"),
	/**  */
	AI_CENTER_BLUE(3, "AI Center B"),
	/**  */
	LOG(1, "Log"),
	/**  */
	BOT_CENTER(2, "Bot Center"),
	/**  */
	VISUALIZER(4, "Visualizer"),
	/**  */
	TIMER(5, "Timer Info"),
	/**  */
	REFEREE(6, "Referee"),
	/**  */
	WP_CENTER(7, "WP Center"),
	/**  */
	CONFIG_EDITOR(8, "Config Editor"),
	/**  */
	PLAYFINDER_STATS(14, "Play Finder Statistics"),
	/**  */
	RCM(31, "RCM"),
	/**  */
	BOT_OVERVIEW(32, "Bot Overview"),
	/** */
	STATISTICS(33, "Game Statistics");
	
	private final int		id;
	private final String	title;
	
	
	private ESumatraViewType(final int id, final String title)
	{
		this.id = id;
		this.title = title;
	}
	
	
	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
	
	
	/**
	 * @return the title
	 */
	public final String getTitle()
	{
		return title;
	}
}
