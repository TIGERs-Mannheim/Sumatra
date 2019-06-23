/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;


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
	void onNewAIInfoFrame(IRecordFrame lastAIInfoframe);
	
	
	/**
	 * AI was disabled. This is called only once
	 * @param teamColor TODO
	 */
	default void onAIStopped(ETeamColor teamColor)
	{
	}
	
	
	/**
	 * This is called whenever a exception occurs during the AI-cycle.
	 * 
	 * @param ex The exception
	 * @param frame The frame and ... (can be <code>null</code>!!!)
	 * @param prevFrame ...the previous frame when the exception occured (can be <code>null</code>!!!)
	 */
	void onAIException(Throwable ex, IRecordFrame frame, IRecordFrame prevFrame);
}
