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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;


/**
 *
 */
public interface IBotManagerObserver
{
	/**
	 * 
	 * @param bot
	 */
	void onBotAdded(ABot bot);
	
	
	/**
	 * 
	 * @param bot
	 */
	void onBotRemoved(ABot bot);
	
	
	/**
	 * 
	 * @param oldId
	 * @param newId
	 */
	void onBotIdChanged(BotID oldId, BotID newId);
	
	
	/**
	 * 
	 * @param bot
	 */
	void onBotConnectionChanged(ABot bot);
}
