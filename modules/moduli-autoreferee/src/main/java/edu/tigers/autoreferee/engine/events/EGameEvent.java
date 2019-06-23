/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 17, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events;

/**
 * @author "Lukas Magel"
 */
public enum EGameEvent
{
	
	/**  */
	BALL_LEFT_FIELD(EEventCategory.VIOLATION),
	/**  */
	BALL_SPEEDING(EEventCategory.VIOLATION),
	/**  */
	DOUBLE_TOUCH(EEventCategory.VIOLATION),
	/**  */
	ATTACKER_TO_DEFENCE_AREA(EEventCategory.VIOLATION),
	/**  */
	BALL_HOLDING(EEventCategory.VIOLATION),
	/**  */
	BOT_COLLISION(EEventCategory.VIOLATION),
	/**  */
	INDIRECT_GOAL(EEventCategory.VIOLATION),
	/**  */
	ICING(EEventCategory.VIOLATION),
	/**  */
	BALL_DRIBBLING(EEventCategory.VIOLATION),
	/**  */
	BOT_COUNT(EEventCategory.VIOLATION),
	/**  */
	BOT_STOP_SPEED(EEventCategory.VIOLATION),
	/**  */
	ATTACKER_IN_DEFENSE_AREA(EEventCategory.VIOLATION),
	/** The defending team comes too close to the ball during a freekick */
	DEFENDER_TO_KICK_POINT_DISTANCE(EEventCategory.VIOLATION),
	/** If the kick was not taken after a certain amount of time */
	KICK_TIMEOUT(EEventCategory.VIOLATION),
	/**  */
	MULTIPLE_DEFENDER(EEventCategory.VIOLATION),
	/**  */
	MULTIPLE_DEFENDER_PARTIALLY(EEventCategory.VIOLATION),
	/**  */
	ATTACKER_TOUCH_KEEPER(EEventCategory.VIOLATION),
	/**  */
	GOAL(EEventCategory.GENERAL);
	
	private final EEventCategory	category;
	
	
	private EGameEvent(final EEventCategory category)
	{
		this.category = category;
	}
	
	
	/**
	 * @return the category
	 */
	public EEventCategory getCategory()
	{
		return category;
	}
}
