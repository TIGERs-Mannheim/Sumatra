/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 28, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;


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
	UNDEFINED;
	
	
	private final ExtIdentifier	defaultMapping	= ExtIdentifier.undefinedIdentifier();
	private final boolean			continuous;
	
	
	private EControllerAction()
	{
		continuous = false;
	}
	
	
	private EControllerAction(final boolean continous)
	{
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
