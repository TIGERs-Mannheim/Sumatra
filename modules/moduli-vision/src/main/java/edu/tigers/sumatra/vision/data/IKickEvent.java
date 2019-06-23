/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * 
 * @author AndreR <andre@ryll.cc> 
 */
public interface IKickEvent
{
	
	/**
	 * @return the position
	 */
	IVector2 getPosition();
	
	
	/**
	 * @return the kickingBot
	 */
	BotID getKickingBot();
	
	
	/**
	 * @return the timestamp
	 */
	long getTimestamp();
	
}