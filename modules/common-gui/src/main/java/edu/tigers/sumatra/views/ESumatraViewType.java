/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import lombok.Getter;


/**
 * Enum containing all views with their ids and title
 * <p>
 * You should not change the ids, because they are used in the saved layouts
 * <p>
 * Please keep the titles unique, too, as atm they will be used for saving the layout
 */
@Getter
public enum ESumatraViewType
{
	AI_CENTER(0, "AI"),
	LOG(1, "Log", true),
	BOT_CENTER(2, "Bot Center"),
	VISUALIZER(4, "Visualizer"),
	TIMER(5, "Timer"),
	REFEREE(6, "Ref"),
	CONFIG_EDITOR(8, "Cfg"),
	DUMMY(9, "<unknown>"),
	RCM(31, "RCM"),
	BOT_OVERVIEW(32, "Bots"),
	OFFENSIVE_STRATEGY(34, "Offensive Strategy"),
	SIMULATION(37, "Simulation"),
	REPLAY_CONTROL(38, "Replay"),
	VISION_ANALYSER(39, "Vision"),
	AUTOREFEREE(40, "AutoReferee"),
	AUTOREFEREE_GAME_LOG(41, "Game Log"),
	BALL_SPEED(42, "Ball Speed"),
	LOGFILE(44, "SSL Logfile"),
	OFFENSIVE_STATISTICS(46, "Offensive Stats"),
	BOT_PARAMS(48, "Bot Params"),
	STATISTICS(49, "Match Stats"),
	OFFENSIVE_ACTION_TREES(50, "Offensive ActionTrees"),
	BALL_KICK_IDENT(51, "Ball & Kick Model"),
	SUPPORT_BEHAVIORS(52, "Support Behaviors"),
	SKILLS(53, "Skills"),
	OFFENSIVE_INTERCEPTIONS(54, "Offensive Interceptions"),

	;

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
}
