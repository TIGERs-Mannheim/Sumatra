/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Dribbler state.
 */
@RequiredArgsConstructor
@Getter
public enum EDribblerState
{
	COLD(0),
	WARM(1),
	HOT(2),
	OVERHEATED(3),
	;

	private final int id;


	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static EDribblerState getDribblerStateConstant(final int id)
	{
		for (EDribblerState s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return COLD;
	}
}
