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

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.ACallback;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * This class should be used to subscribe to special topics and which forwards messages on these topics to the right
 * objects.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public final class Receiving extends ACallback implements IReceiving
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log	= Logger.getLogger(ACallback.class.getName());
	
	private final Map<ETopics, AMessageNotifier>	receiveCallbacks;
	private MessageType									type;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public Receiving(MessageType type)
	{
		super();
		receiveCallbacks = new Hashtable<ETopics, AMessageNotifier>();
		this.type = type;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage)
	{
		ETopics etopic = ETopics.getTopicByName(topic);
		if (receiveCallbacks.containsKey(etopic))
		{
			final AMessageNotifier receivers = receiveCallbacks.get(etopic);
			receivers.messageArrived(mqttMessage);
		}
	}
	
	
	/**
	 * @param topic
	 * @param messageReceiver
	 */
	@Override
	public void addMessageReceiver(ETopics topic, IMessageReceivable messageReceiver)
	{
		if (receiveCallbacks.containsKey(topic))
		{
			receiveCallbacks.get(topic).addMessageReceiver(messageReceiver);
		} else
		{
			final AMessageNotifier receivers = type.getMessageNotifier(topic);
			receivers.addMessageReceiver(messageReceiver);
			receiveCallbacks.put(topic, receivers);
			super.subscribe(topic.getName(), topic.getType().getQos());
		}
		log.info("subscribed to " + topic);
	}
	
	
	/**
	 * @param topic
	 * @param messageReceiver
	 */
	@Override
	public void removeMessageReceiver(ETopics topic, IMessageReceivable messageReceiver)
	{
		if (receiveCallbacks.containsKey(topic))
		{
			final AMessageNotifier receivers = receiveCallbacks.get(topic);
			receivers.removeMessageReceiver(messageReceiver);
			if (receivers.isEmpty())
			{
				receiveCallbacks.remove(topic);
				super.unsubscribe(topic.getName());
				receivers.close();
			}
		}
	}
	
	
	@Override
	public boolean connect()
	{
		boolean state = super.connect();
		for (AMessageNotifier msgNot : receiveCallbacks.values())
		{
			msgNot.onConnectionEvent(state);
		}
		return state;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
