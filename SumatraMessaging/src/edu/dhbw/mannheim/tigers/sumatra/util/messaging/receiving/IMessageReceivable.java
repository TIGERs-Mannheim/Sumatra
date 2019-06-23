/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 19, 2012
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.IConnectionStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * This interface should all classes implement, that want to be informed about new messages on their subscribed Topic.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public interface IMessageReceivable extends IConnectionStateObserver
{
	/**
	 * 
	 * 
	 * @param mqttTopic
	 * @param message
	 */
	void messageArrived(ETopics mqttTopic, Message message);
}
