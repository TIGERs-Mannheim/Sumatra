/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EDribbleTestMovement
{
	BACKWARDS_TURN(1),
	FORWARD_TURN(1),
	TURN_180(3),
	TURN_LEFT(0),
	TURN_RIGHT(0),
	SIDEWAYS(4),
	SIDEWAYS_TURN(4),
	/**
	 * NONE should always be last
	 */
	NONE(0);

	private final int repetitions;
}
