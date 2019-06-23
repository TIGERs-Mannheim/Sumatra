/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

/**
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface IAnimationTimer
{
	/**
	 * Get current timer value.
	 * 
	 * @return Value in the range of [0.0, 1.0]
	 */
	float getTimerValue();
}
