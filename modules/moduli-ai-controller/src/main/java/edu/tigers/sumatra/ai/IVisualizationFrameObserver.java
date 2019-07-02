/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ids.EAiTeam;


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
	default void onClearVisualizationFrame(final EAiTeam teamColor)
	{
	}
}
