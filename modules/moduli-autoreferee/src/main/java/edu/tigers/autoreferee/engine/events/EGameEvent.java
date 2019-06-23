/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.MessagesRobocupSslGameEvent.SSL_Referee_Game_Event.GameEventType;


/**
 * AutoRef game events and mapping to human readable texts and referee protocol enum
 */
public enum EGameEvent
{
	NUMBER_OF_PLAYERS(EEventCategory.VIOLATION, "Robot Count", GameEventType.NUMBER_OF_PLAYERS),
	BALL_LEFT_FIELD(EEventCategory.VIOLATION, "Ball left the field", GameEventType.BALL_LEFT_FIELD),
	GOAL(EEventCategory.GENERAL, "Goal", GameEventType.GOAL),
	KICK_TIMEOUT(EEventCategory.VIOLATION, "Kick Timeout", GameEventType.KICK_TIMEOUT),
	NO_PROGRESS_IN_GAME(EEventCategory.VIOLATION, "No Progress in Game", GameEventType.NO_PROGRESS_IN_GAME),
	BOT_COLLISION(EEventCategory.VIOLATION, "Bot Collision", GameEventType.BOT_COLLISION),
	MULTIPLE_DEFENDER(EEventCategory.VIOLATION, "Multiple Defender", GameEventType.MULTIPLE_DEFENDER),
	MULTIPLE_DEFENDER_PARTIALLY(EEventCategory.VIOLATION, "Multiple Defender Partially",
			GameEventType.MULTIPLE_DEFENDER_PARTIALLY),
	ATTACKER_IN_DEFENSE_AREA(EEventCategory.VIOLATION, "Attacker in Defense Area",
			GameEventType.ATTACKER_IN_DEFENSE_AREA),
	ICING(EEventCategory.VIOLATION, "Icing", GameEventType.ICING),
	BALL_SPEED(EEventCategory.VIOLATION, "Ball Speeding", GameEventType.BALL_SPEED),
	ROBOT_STOP_SPEED(EEventCategory.VIOLATION, "Robot Stop Speed", GameEventType.ROBOT_STOP_SPEED),
	BALL_DRIBBLING(EEventCategory.VIOLATION, "Dribbling", GameEventType.BALL_DRIBBLING),
	ATTACKER_TOUCH_KEEPER(EEventCategory.VIOLATION, "Attacker Touched Keeper", GameEventType.ATTACKER_TOUCH_KEEPER),
	DOUBLE_TOUCH(EEventCategory.VIOLATION, "Double Touch", GameEventType.DOUBLE_TOUCH),
	ATTACKER_TO_DEFENCE_AREA(EEventCategory.VIOLATION, "Attacker too close to Defense Area",
			GameEventType.ATTACKER_TO_DEFENCE_AREA),
	DEFENDER_TO_KICK_POINT_DISTANCE(EEventCategory.VIOLATION, "Robot to Ball Distance",
			GameEventType.DEFENDER_TO_KICK_POINT_DISTANCE),
	BALL_HOLDING(EEventCategory.VIOLATION, "Ball Holding", GameEventType.BALL_HOLDING),
	INDIRECT_GOAL(EEventCategory.VIOLATION, "Indirect Goal", GameEventType.INDIRECT_GOAL),
	BALL_PLACEMENT_FAILED(EEventCategory.VIOLATION, "Ball placement not successful",
			GameEventType.BALL_PLACEMENT_FAILED),
	CHIP_ON_GOAL(EEventCategory.VIOLATION, "Ball was chipped on goal", GameEventType.CHIP_ON_GOAL),
	MULTIPLE_YELLOW_CARDS(EEventCategory.VIOLATION, "Team got multiple yellow cards", GameEventType.CUSTOM),
	
	;
	
	
	private final EEventCategory category;
	private final String eventText;
	private final GameEventType gameEventType;
	
	
	EGameEvent(final EEventCategory category, final String eventText, final GameEventType gameEventType)
	{
		this.category = category;
		this.eventText = eventText;
		this.gameEventType = gameEventType;
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
	
	
	public GameEventType getGameEventType()
	{
		return gameEventType;
	}
}
