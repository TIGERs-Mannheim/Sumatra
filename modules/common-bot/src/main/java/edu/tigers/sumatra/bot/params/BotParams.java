/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.bot.params;

import com.sleepycat.persist.model.Persistent;


/**
 * Data holder for all parameters of a robot.
 * Includes movement limits and physical properties.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class BotParams implements IBotParams
{
	protected BotMovementLimits	movementLimits	= new BotMovementLimits();
	protected BotDimensions			dimensions		= new BotDimensions();
	protected BotKickerSpecs		kickerSpecs		= new BotKickerSpecs();
	
	
	/**
	 * @return the movementLimits
	 */
	@Override
	public IBotMovementLimits getMovementLimits()
	{
		return movementLimits;
	}
	
	
	/**
	 * @return the dimensions
	 */
	@Override
	public IBotDimensions getDimensions()
	{
		return dimensions;
	}
	
	
	/**
	 * @return the kickerSpecs
	 */
	@Override
	public IBotKickerSpecs getKickerSpecs()
	{
		return kickerSpecs;
	}
}
