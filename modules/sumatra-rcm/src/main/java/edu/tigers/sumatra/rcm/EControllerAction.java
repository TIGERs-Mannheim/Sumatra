/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Actions that can be mapped to controllers
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum EControllerAction
{
	FORWARD(true),
	BACKWARD(true),
	LEFT(true),
	RIGHT(true),
	ROTATE_LEFT(true),
	ROTATE_RIGHT(true),
	DRIBBLE(true),
	DISARM,
	KICK_ARM,
	KICK_FORCE,
	CHIP_ARM,
	CHIP_FORCE,
	ACCELERATE(true),
	DECELERATE(true),
	UNDEFINED;


	private final boolean continuous;


	EControllerAction()
	{
		continuous = false;
	}
}
