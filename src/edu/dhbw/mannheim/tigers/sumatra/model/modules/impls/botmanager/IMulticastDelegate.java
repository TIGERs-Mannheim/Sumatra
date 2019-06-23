/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;


/**
 * Interface for multicast commands and management.
 * 
 * @author AndreR
 * 
 */
public interface IMulticastDelegate
{
	/**
	 * 
	 * @param botId
	 * @param move
	 */
	void setGroupedMove(BotID botId, TigerMotorMoveV2 move);
	
	
	/**
	 * 
	 * @param botId
	 * @param kick
	 */
	void setGroupedKick(BotID botId, TigerKickerKickV2 kick);
	
	
	/**
	 * 
	 * @param botId
	 * @param dribble
	 */
	void setGroupedDribble(BotID botId, TigerDribble dribble);
	
	
	/**
	 * 
	 * @param bot
	 */
	void setIdentity(TigerBot bot);
	
	
	/**
	 * 
	 * @param bot
	 */
	void removeIdentity(TigerBot bot);
	
	
	/**
	 * 
	 * @param botId
	 * @param enable
	 * @return
	 */
	boolean setMulticast(BotID botId, boolean enable);
}
