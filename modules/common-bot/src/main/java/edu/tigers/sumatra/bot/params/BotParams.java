/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
	private final BotMovementLimits movementLimits = new BotMovementLimits();
	private final BotDimensions dimensions = new BotDimensions();
	private final BotKickerSpecs kickerSpecs = new BotKickerSpecs();
	private double feedbackDelay;
	
	
	@Override
	public double getFeedbackDelay()
	{
		return feedbackDelay;
	}
	
	
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
	
	
	public void setFeedbackDelay(final double feedbackDelay)
	{
		this.feedbackDelay = feedbackDelay;
	}
}
