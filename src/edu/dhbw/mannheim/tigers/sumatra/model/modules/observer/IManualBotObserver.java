/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 *
 */
public interface IManualBotObserver
{
	/**
	 * 
	 * @param bot
	 */
	void onManualBotAdded(BotID bot);
	
	
	/**
	 * 
	 * @param bot
	 */
	void onManualBotRemoved(BotID bot);
}
