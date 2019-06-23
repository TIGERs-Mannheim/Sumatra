/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.BotPopUpMenu.IBotPopUpMenuObserver;


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
