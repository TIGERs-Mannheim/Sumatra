/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.visualizer.BotPopUpMenu.IBotPopUpMenuObserver;


/**
 * FieldPanel observer interface.
 * 
 * @author AndreR
 */
public interface IRobotsPanelObserver extends IBotPopUpMenuObserver
{
	/**
	 * @param botId
	 */
	default void onRobotClick(final BotID botId)
	{
	}
}
