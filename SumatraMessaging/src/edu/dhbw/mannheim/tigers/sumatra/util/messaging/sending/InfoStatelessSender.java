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

import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * creates a list for pending publishes and only send newest, forget others
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class InfoStatelessSender extends AMessageSender
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param mqttMessage
	 * @param tokens
	 * @param sender
	 */
	@Override
	public void publish(MqttMessage mqttMessage, Map<MqttDeliveryToken, IMessageSender> tokens, IMessageSender sender)
	{
		for (Future<MqttDeliveryToken> f : getThreadResults())
		{
			f.cancel(true);
		}
		getThreadResults().clear();
		super.publish(mqttMessage, tokens, sender);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
