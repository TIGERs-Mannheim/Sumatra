/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.visualizer.view.BotPopUpMenu.IBotPopUpMenuObserver;


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
