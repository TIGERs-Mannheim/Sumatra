/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IVisualizationFrameObserver
{
	/**
	 * @param frame
	 */
	default void onNewVisualizationFrame(final VisualizationFrame frame)
	{
	}
	
	
	/**
	 * @param teamColor
	 */
	default void onClearVisualizationFrame(final ETeamColor teamColor)
	{
	}
	
	
	/**
	 * This is called whenever a exception occurs during the AI-cycle.
	 * 
	 * @param ex The exception
	 * @param frame The frame and ... (can be <code>null</code>!!!)
	 * @param prevFrame ...the previous frame when the exception occured (can be <code>null</code>!!!)
	 */
	default void onAIException(final Throwable ex, final VisualizationFrame frame, final VisualizationFrame prevFrame)
	{
	}
}
