/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;


/**
 * This interface is used to visualize AI decisions and path planing informations.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public interface IAIObserver
{
	/**
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * 
	 * @param lastAIInfoframe
	 */
	default void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
	}
}
