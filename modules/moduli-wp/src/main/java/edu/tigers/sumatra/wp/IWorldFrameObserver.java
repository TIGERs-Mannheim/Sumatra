/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Gero
 */
public interface IWorldFrameObserver
{
	/**
	 * A new worldframe is coming in. Note that there is a worldframe for each team color,
	 * so you may need to sort some out
	 * 
	 * @param wFrameWrapper
	 */
	default void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
	}
	
	
	/**
	 * This is called if the WP is stopped (Sumatra closes/stops)
	 */
	default void onClearWorldFrame()
	{
	}
	
	
	/**
	 * @param frame
	 */
	default void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
	}
	
	
	/**
	 * Vision lost. Clear your state.
	 */
	default void onClearCamDetectionFrame()
	{
	}
}
