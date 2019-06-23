/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

/**
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface INumberAnimator
{
	/**
	 * Get the current number.
	 * 
	 * @return
	 */
	float getNumber();
}
