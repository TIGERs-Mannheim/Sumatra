/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

/**
 * @author "Lukas Magel"
 */
public enum EGameEvent
{
	
	/**  */
	BALL_LEFT_FIELD(EEventCategory.VIOLATION, "Ball left the field"),
	/**  */
	BALL_SPEEDING(EEventCategory.VIOLATION, "Ball Speeding"),
	/**  */
	DOUBLE_TOUCH(EEventCategory.VIOLATION, "Double Touch"),
	/**  */
	ATTACKER_TO_DEFENCE_AREA(EEventCategory.VIOLATION, "Attacker too close to Defense Area"),
	/**  */
	BALL_HOLDING(EEventCategory.VIOLATION, "Ball Holding"),
	/**  */
	BOT_COLLISION(EEventCategory.VIOLATION, "Bot Collision"),
	/**  */
	INDIRECT_GOAL(EEventCategory.VIOLATION, "Indirect Goal"),
	/**  */
	ICING(EEventCategory.VIOLATION, "Icing"),
	/**  */
	BALL_DRIBBLING(EEventCategory.VIOLATION, "Dribbling"),
	/**  */
	BOT_COUNT(EEventCategory.VIOLATION, "Robot Count"),
	/**  */
	BOT_STOP_SPEED(EEventCategory.VIOLATION, "Robot Stop Speed"),
	/**  */
	ATTACKER_IN_DEFENSE_AREA(EEventCategory.VIOLATION, "Attacker in Defense Area"),
	/** The defending team comes too close to the ball during a freekick */
	DEFENDER_TO_KICK_POINT_DISTANCE(EEventCategory.VIOLATION, "Robot to Ball Distance"),
	/** If the kick was not taken after a certain amount of time */
	KICK_TIMEOUT(EEventCategory.VIOLATION, "Kick Timeout"),
	/** If there is no progress in game */
	NO_PROGRESS_IN_GAME(EEventCategory.VIOLATION, "No Progress in Game"),
	/**  */
	MULTIPLE_DEFENDER(EEventCategory.VIOLATION, "Multiple Defender"),
	/**  */
	MULTIPLE_DEFENDER_PARTIALLY(EEventCategory.VIOLATION, "Multiple Defender Partially"),
	/**  */
	ATTACKER_TOUCH_KEEPER(EEventCategory.VIOLATION, "Attacker Touched Keeper"),
	/**  */
	GOAL(EEventCategory.GENERAL, "Goal");
	
	private final EEventCategory category;
	private final String eventText;
	
	
	EGameEvent(final EEventCategory category, final String eventText)
	{
		this.category = category;
		this.eventText = eventText;
	}
	
	
	/**
	 * @return the category
	 */
	public EEventCategory getCategory()
	{
		return category;
	}
	
	
	public String getEventText()
	{
		return eventText;
	}
}
