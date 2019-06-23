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
import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;


/**
 * Class which publishes a message on a topic.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class SendMessage implements Callable<MqttDeliveryToken>
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final MqttMessage										message;
	private final MqttTopic											topic;
	private final Map<MqttDeliveryToken, IMessageSender>	tokens;
	private final IMessageSender									sender;
	private boolean													hasCallback;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param topic
	 * @param message
	 * @param tokens
	 * @param sender
	 */
	public SendMessage(MqttTopic topic, MqttMessage message, Map<MqttDeliveryToken, IMessageSender> tokens,
			IMessageSender sender)
	{
		this.topic = topic;
		this.message = message;
		this.tokens = tokens;
		this.sender = sender;
		if ((tokens != null) && (sender != null))
		{
			hasCallback = true;
		} else
		{
			hasCallback = false;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public MqttDeliveryToken call() throws MqttException
	{
		MqttDeliveryToken token = topic.publish(message);
		if (hasCallback)
		{
			tokens.put(token, sender);
		}
		return token;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
