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
	AI_CENTER_YELLOW(0, "AIY"),
	/**  */
	AI_CENTER_BLUE(3, "AIB"),
	/**  */
	LOG(1, "Log"),
	/**  */
	BOT_CENTER(2, "Bot Center"),
	/**  */
	VISUALIZER(4, "Visualizer"),
	/**  */
	TIMER(5, "Timer"),
	/**  */
	REFEREE(6, "Ref"),
	/**  */
	WP_CENTER(7, "WP"),
	/**  */
	CONFIG_EDITOR(8, "Cfg"),
	/**  */
	PLAYFINDER_STATS(14, "Play Finder Statistics"),
	/**  */
	RCM(31, "RCM"),
	/**  */
	BOT_OVERVIEW(32, "Bots"),
	/** */
	STATISTICS_YELLOW(33, "Stats Y"),
	/** */
	STATISTICS_BLUE(36, "Stats B"),
	/** */
	OFFENSIVE_STRATEGY(34, "Offensive Strategy"),
	/**  */
	BOT_CENTER_V2(35, "Bot Center"),
	/**  */
	SIMULATION(37, "Simulation"),
	/**  */
	REPLAY_CONTROL(38, "Replay"),
	/**  */
	BALL_ANALYSER(39, "Ball");
	
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
