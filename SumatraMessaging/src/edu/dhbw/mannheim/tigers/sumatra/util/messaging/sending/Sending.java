/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 19, 2012
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.ACallback;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * This class is used for sending messages to a MQTT broker.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class Sending extends ACallback implements ISending
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Map<String, AMessageSender>			sendCallbacks;
	private final MessageType								type;
	
	
	private Map<MqttDeliveryToken, IMessageSender>	tokens;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public Sending(MessageType type)
	{
		super();
		sendCallbacks = new Hashtable<String, AMessageSender>();
		this.type = type;
		tokens = new ConcurrentHashMap<MqttDeliveryToken, IMessageSender>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		IMessageSender sender = tokens.remove(token);
		if (sender != null)
		{
			try
			{
				sender.deliveryComplete(token.getMessage());
			} catch (MqttException err)
			{
				sender.deliveryComplete();
			}
		}
	}
	
	
	/**
	 * @param topic
	 * @param message
	 * @param sender
	 * @return true if publish request is forqarded to sender, false if not connected
	 */
	@Override
	public boolean publish(ETopics topic, byte[] message, IMessageSender sender)
	{
		if (isConnected())
		{
			final AMessageSender messageSender;
			if (sendCallbacks.containsKey(topic.getName()))
			{
				messageSender = sendCallbacks.get(topic.getName());
			} else
			{
				final MqttTopic mqttTopic = getMqtt().getTopic(topic.getName());
				messageSender = type.getMessageSender();
				messageSender.setTopic(mqttTopic);
				sendCallbacks.put(topic.getName(), messageSender);
			}
			
			final MqttMessage mqttMessage = new MqttMessage(message);
			mqttMessage.setQos(topic.getType().getQos());
			mqttMessage.setRetained(topic.getType().isRetained());
			messageSender.publish(mqttMessage, tokens, sender);
			
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean publish(ETopics topic, byte[] message)
	{
		return this.publish(topic, message, null);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
