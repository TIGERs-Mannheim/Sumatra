/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.07.2016
 * Author(s): ryll
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.botskills.data;

/**
 * Selectable songs, playable on the robots.
 */
public enum ESong
{
	NONE(0),
	CHEERING(1),
	FINAL_COUNTDOWN(2),
	ELEVATOR(3),
	EYE_OF_THE_TIGER_1(4),
	EYE_OF_THE_TIGER_2(5),
	PLACEHOLDER_1(6),
	PLACEHOLDER_2(7),
	
	;
	
	private final int id;
	
	
	ESong(final int id)
	{
		this.id = id;
	}
	
	
	public final int getId()
	{
		return id;
	}
}
