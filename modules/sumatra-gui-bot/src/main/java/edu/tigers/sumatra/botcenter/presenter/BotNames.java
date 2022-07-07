/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botcenter.presenter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * List of all bot names
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BotNames
{
	private static final List<String> BOT_NAMES = List.of(
			"Gandalf",
			"Chosen One",
			"Eichbaum",
			"This Bot",
			"Wolpertinger",
			"JARVIS",
			"Tigger",
			"Black Widow",
			"Thor",
			"HAL 9000",
			"Lovelace",
			"Athena",
			"Roboter",
			"Dijkstra",
			"Torvalds",
			"Curie"
	);


	public static String get(int id)
	{
		return BOT_NAMES.get(id);
	}
}
