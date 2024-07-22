/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum EBallObservationState
{
	/**
	 * Ball was not observed for some time
	 */
	UNKNOWN(0),
	/**
	 * Ball state is only a predicition
	 */
	DEAD_RECKONING(1),
	/**
	 * Ball was detected far away, usually by camera
	 */
	FAR_DETECTION(2),
	/**
	 * Ball was detected at IR barrier
	 */
	AT_BARRIER(3),
	/**
	 * Dribbling bar is actively exerting force onto the ball
	 */
	ACTIVE_DRIBBLING(4);

	private final int id;


	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static EBallObservationState getBallObservationStateConstant(final int id)
	{
		for (EBallObservationState s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return UNKNOWN;
	}
}
