/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;


/**
 * Actions that can be mapped to controllers
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EControllerAction
{
	/**  */
	FORWARD(true),
	/**  */
	BACKWARD(true),
	/**  */
	LEFT(true),
	/**  */
	RIGHT(true),
	/**  */
	ROTATE_LEFT(true),
	/**  */
	ROTATE_RIGHT(true),
	/**  */
	DRIBBLE(true),
	/**  */
	DISARM,
	/**  */
	KICK_ARM,
	/**  */
	KICK_FORCE,
	/**  */
	CHIP_ARM,
	/**  */
	CHIP_FORCE,
	
	/**  */
	ACCELERATE(true),
	/**  */
	DECELERATE(true),
	/**  */
	UNDEFINED;
	
	
	private final ExtIdentifier	defaultMapping	= ExtIdentifier.undefinedIdentifier();
	private final boolean			continuous;


	EControllerAction() {
		continuous = false;
	}


	EControllerAction(final boolean continous) {
		continuous = continous;
	}
	
	
	/**
	 * @return the defaultMapping
	 */
	public ExtIdentifier getDefaultMapping()
	{
		return defaultMapping;
	}
	
	
	/**
	 * @return the continuous
	 */
	public boolean isContinuous()
	{
		return continuous;
	}
}
