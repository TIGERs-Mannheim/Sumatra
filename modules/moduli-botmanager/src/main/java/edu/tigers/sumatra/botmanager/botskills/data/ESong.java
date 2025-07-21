/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills.data;

import lombok.Getter;


/**
 * Selectable songs, playable on the robots.
 */
public enum ESong
{
	NONE(0, 0, false),
	CHEERING(1, 0.64, true), // UPDOWN_20
	FINAL_COUNTDOWN(2, 17.106, false),
	TETRIS(3, 41.643, false),
	EYE_OF_THE_TIGER_LEAD(4, 8.88, false),
	EYE_OF_THE_TIGER_FOLLOW(5, 8.88, false),
	CANTINA_BAND(6, 57.9, false),
	MACARENA(7, 9.6, false),
	IMPERIAL_MARCH(8, 27.569, false),

	;

	@Getter
	private final int id;

	@Getter
	private final double duration; // In seconds

	@Getter
	private final boolean isLooping;


	ESong(int id, double duration, boolean isLooping)
	{
		this.id = id;
		this.duration = duration;
		this.isLooping = isLooping;
	}


	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static ESong getSongConstant(final int id)
	{
		for (ESong s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return null;
	}
}
