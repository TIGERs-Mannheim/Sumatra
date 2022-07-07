/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;

import com.sleepycat.persist.model.Persistent;


/**
 * Data holder for all parameters of a robot.
 * Includes movement limits and physical properties.
 */
@Persistent
public class BotParams implements IBotParams
{
	private final BotMovementLimits movementLimits = new BotMovementLimits();
	private final BotDimensions dimensions = new BotDimensions();
	private final BotKickerSpecs kickerSpecs = new BotKickerSpecs();
	private final BotDribblerSpecs dribblerSpecs = new BotDribblerSpecs();


	@Override
	public IBotMovementLimits getMovementLimits()
	{
		return movementLimits;
	}


	@Override
	public IBotDimensions getDimensions()
	{
		return dimensions;
	}


	@Override
	public IBotKickerSpecs getKickerSpecs()
	{
		return kickerSpecs;
	}


	@Override
	public BotDribblerSpecs getDribblerSpecs()
	{
		return dribblerSpecs;
	}
}
