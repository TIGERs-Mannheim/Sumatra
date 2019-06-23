/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.IConnection;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface ISending extends IConnection
{
	/**
	 * @param topic
	 * @param message
	 * @param sender
	 * @return
	 */
	boolean publish(ETopics topic, byte[] message, IMessageSender sender);
	
	
	/**
	 * 
	 * @param topic
	 * @param message
	 * @return
	 */
	boolean publish(ETopics topic, byte[] message);
}
