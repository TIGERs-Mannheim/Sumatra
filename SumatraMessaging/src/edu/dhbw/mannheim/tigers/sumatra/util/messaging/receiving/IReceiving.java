/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.IConnection;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IReceiving extends IConnection
{
	/**
	 * @param topic
	 * @param messageReceiver
	 */
	void addMessageReceiver(ETopics topic, IMessageReceivable messageReceiver);
	
	
	/**
	 * @param topic
	 * @param messageReceiver
	 */
	void removeMessageReceiver(ETopics topic, IMessageReceivable messageReceiver);
}
