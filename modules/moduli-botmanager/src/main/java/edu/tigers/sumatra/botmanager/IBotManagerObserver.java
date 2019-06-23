/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.ABot;


/**
 * Bot manager observer.
 */
public interface IBotManagerObserver
{
	/**
	 * @param bot
	 */
	void onBotAdded(ABot bot);
	
	
	/**
	 * @param bot
	 */
	void onBotRemoved(ABot bot);
}
