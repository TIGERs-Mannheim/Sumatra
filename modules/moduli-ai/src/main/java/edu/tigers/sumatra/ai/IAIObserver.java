/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ids.EAiTeam;


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
	
	
	/**
	 * @param aiTeam
	 * @param mode
	 */
	default void onAiModeChanged(final EAiTeam aiTeam, final EAIControlState mode)
	{
	}
}
