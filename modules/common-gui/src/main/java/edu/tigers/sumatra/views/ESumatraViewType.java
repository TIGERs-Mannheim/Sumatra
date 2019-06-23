/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

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
	AI_CENTER(0, "AI"),
	/** */
	@Deprecated
	AI_CENTER_BLUE_OLD(3, "AIB"),
	/**  */
	LOG(1, "Log", true),
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
	/** */
	DUMMY(9, "<unknown>"),
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
	VISION_ANALYSER(39, "Vision"),
	/**  */
	AUTOREFEREE(40, "AutoReferee"),
	/**  */
	AUTOREFEREE_GAME_LOG(41, "Game Log", true),
	/**  */
	BALL_SPEED(42, "Ball Speed", true),
	/**  */
	HUMAN_REF_VIEW(43, "Human Ref View"),
	/**  */
	LOGFILE(44, "SSL Logfile"),
	/**  */
	TEST_PLAYS(45, "Test plays"),
	/** */
	OFFENSIVE_STATISTICS(46, "Offensive Stats"),
	/** */
	MATCH_COMMANDS(47, "Match Commands"),
	/** */
	BOT_PARAMS(48, "Bot Params"),
	/** */
	STATISTICS(49, "Match Stats");
	
	private final int id;
	private final String title;
	private final boolean forceLoad;
	
	
	ESumatraViewType(final int id, final String title)
	{
		this(id, title, false);
	}
	
	
	ESumatraViewType(final int id, final String title, final boolean forceLoad)
	{
		this.id = id;
		this.title = title;
		this.forceLoad = forceLoad;
	}
	
	
	/**
	 * Get type by id
	 * 
	 * @param id
	 * @return
	 */
	public static ESumatraViewType fromId(final int id)
	{
		for (ESumatraViewType type : values())
		{
			if (type.id == id)
			{
				return type;
			}
		}
		return null;
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
	
	
	/**
	 * @return the forceLoad
	 */
	public final boolean isForceLoad()
	{
		return forceLoad;
	}
}
