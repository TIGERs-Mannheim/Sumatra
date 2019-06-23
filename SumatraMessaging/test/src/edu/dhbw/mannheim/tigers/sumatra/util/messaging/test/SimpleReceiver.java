/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.ACallback;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IReceiving;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class SimpleReceiver extends ACallback implements IReceiving
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Map<ETopics, List<IMessageReceivable>>	receiveCallbacks;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param host
	 * @param port
	 * @throws MqttException
	 */
	public SimpleReceiver(String host, int port) throws MqttException
	{
		super();
		setHostPort(host, port);
		connect();
		receiveCallbacks = new Hashtable<ETopics, List<IMessageReceivable>>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage)
	{
		try
		{
			ETopics etopic = ETopics.getTopicByName(topic);
			if (receiveCallbacks.containsKey(etopic))
			{
				Builder<?> builder = (Builder<?>) etopic.getProtoType();
				Message message;
				
				message = builder.mergeFrom(mqttMessage.getPayload()).build();
				
				final List<IMessageReceivable> receivers = receiveCallbacks.get(etopic);
				for (IMessageReceivable rcv : receivers)
				{
					rcv.messageArrived(etopic, message);
				}
			}
		} catch (InvalidProtocolBufferException err)
		{
			err.printStackTrace();
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
			receiveCallbacks.get(topic).add(messageReceiver);
		} else
		{
			final List<IMessageReceivable> receivers = new ArrayList<IMessageReceivable>();
			receivers.add(messageReceiver);
			receiveCallbacks.put(topic, receivers);
			super.subscribe(topic.getName(), topic.getType().getQos());
		}
	}
	
	
	/**
	 * @deprecated not implemented
	 */
	@Deprecated
	@Override
	public void removeMessageReceiver(ETopics topic, IMessageReceivable messageReceiver)
	{
		// NOT IMPLEMENTED
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
