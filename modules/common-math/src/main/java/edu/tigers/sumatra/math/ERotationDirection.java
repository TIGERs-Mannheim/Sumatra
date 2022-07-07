/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

public enum ERotationDirection
{
	NONE,
	CLOCKWISE,
	COUNTER_CLOCKWISE;


	public ERotationDirection opposite()
	{
		switch (this)
		{
			case CLOCKWISE:
				return COUNTER_CLOCKWISE;
			case COUNTER_CLOCKWISE:
				return CLOCKWISE;
			default:
				return NONE;
		}
	}
}
