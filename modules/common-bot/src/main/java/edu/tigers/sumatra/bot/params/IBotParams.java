/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.bot.params;


/**
 * 
 * @author AndreR <andre@ryll.cc> 
 */
public interface IBotParams
{
	
	/**
	 * @return the movementLimits
	 */
	IBotMovementLimits getMovementLimits();
	
	
	/**
	 * @return the dimensions
	 */
	IBotDimensions getDimensions();
	
	
	/**
	 * @return the kickerSpecs
	 */
	IBotKickerSpecs getKickerSpecs();
	
}