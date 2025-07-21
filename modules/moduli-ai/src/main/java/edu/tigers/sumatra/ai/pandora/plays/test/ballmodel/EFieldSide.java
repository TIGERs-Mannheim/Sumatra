/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.ballmodel;

import lombok.Getter;


public enum EFieldSide
{
	OUR(-1),
	BOTH(0),
	THEIR(1);

	@Getter
	private final int sign;

	EFieldSide(int sign)
	{
		this.sign = sign;
	}
}
