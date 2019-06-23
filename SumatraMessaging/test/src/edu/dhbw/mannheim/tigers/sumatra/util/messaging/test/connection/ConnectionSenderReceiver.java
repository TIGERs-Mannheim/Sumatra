/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.connection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.IMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.TestProtos;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * Receving class for the connection test
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ConnectionSenderReceiver implements IMessageReceivable, IMessageSender
{
	
	private Map<Integer, Boolean>	messagesArrived	= new HashMap<Integer, Boolean>();
	
	
	@Override
	public void onConnectionEvent(boolean connected)
	{
	}
	
	
	@Override
	public void deliveryComplete(MqttMessage message)
	{
	}
	
	
	@Override
	public void deliveryComplete()
	{
	}
	
	
	@Override
	public void messageArrived(ETopics mqttTopic, Message message)
	{
		switch (mqttTopic)
		{
			case TEST_RECONNECT_PUBLISH:
				TestProtos.ConnectionInfo msg = (TestProtos.ConnectionInfo) message;
				messagesArrived.put(msg.getId(), true);
				break;
			default:
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param id
	 * @return
	 */
	public boolean isMessageArrived(int id)
	{
		if (messagesArrived.containsKey(id))
		{
			return messagesArrived.get(id);
		}
		return false;
	}
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
