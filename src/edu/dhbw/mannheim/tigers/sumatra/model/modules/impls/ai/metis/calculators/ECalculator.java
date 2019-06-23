/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;


/**
 * This Enum contains all calculators by name
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public enum ECalculator
{
	/** */
	BOT_TO_BALL_DISTANCE_TIGERS,
	/** */
	BOT_TO_BALL_DISTANCE_OPPONENTS,
	/** */
	DEFENSE_HELPER,
	/** */
	DEFENSE_POINTS,
	/** */
	BALL_POSSESSION,
	/** */
	POSSIBLE_GOAL,
	/** */
	TEAM_CLOSEST_TO_BALL,
	/** */
	SCORING_CHANCE_TIGERS,
	/** */
	SCORING_CHANCE_OPPONENTS,
	/** */
	APPROXIMATE_SCORING_CHANCE_TIGERS,
	/** */
	APPROXIMATE_SCORING_CHANCE_OPOONENTS,
	/** */
	OFFENSE_POINTS_CARRIER(false),
	/** */
	OFFENSE_POINTS_RECEIVER_LEFT(false),
	/** */
	OFFENSE_POINTS_RECEIVER_RIGHT(false),
	/** */
	BOT_LAST_TOUCHED_BALL,
	/** */
	BOT_NOT_ALLOWED_TO_TOUCH_BALL,
	/** */
	DANGEROUS_OPPONENTS,
	/** */
	FIELD_ANALYSER,
	/** */
	PLAY_PATTERN_DETECT,
	/**  */
	OTHER_MIXED_TEAM_TOUCH,
	/**  */
	SHOOTER_MEMORY,
	/**  */
	BALL_KICK_LEARNING(false),
	/** */
	FORCE_AFTER_KICKOFF;
	
	private final boolean	initiallyActive;
	
	
	private ECalculator()
	{
		this(true);
	}
	
	
	private ECalculator(boolean active)
	{
		initiallyActive = active;
	}
	
	
	/**
	 * @return the initiallyActive
	 */
	public final boolean isInitiallyActive()
	{
		return initiallyActive;
	}
}
