/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 2, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.IConnectionStateObserver;


/**
 * This interface should be implemented when a class sends data.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IMessageSender extends IConnectionStateObserver
{
	
	/**
	 * @param message
	 */
	void deliveryComplete(MqttMessage message);
	
	
	/**
	 */
	void deliveryComplete();
	
}
