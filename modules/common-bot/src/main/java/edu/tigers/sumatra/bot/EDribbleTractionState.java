/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum EDribbleTractionState
{
	/**
	 * Dribbler is off
	 */
	OFF(0),
	/**
	 * Dribbler is on but has no ball contact
	 */
	IDLE(1),
	/**
	 * Dribbler has ball contact but exerted force is low
	 */
	LIGHT(2),
	/**
	 * Dribbler is exerting strong force onto the ball
	 */
	STRONG(3),
	;

	private final int id;


	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static EDribbleTractionState getDribbleTractionStateConstant(final int id)
	{
		for (EDribbleTractionState s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return OFF;
	}
}
