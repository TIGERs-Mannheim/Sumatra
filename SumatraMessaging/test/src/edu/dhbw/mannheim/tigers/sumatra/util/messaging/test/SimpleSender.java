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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.ACallback;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.IMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.ISending;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class SimpleSender extends ACallback implements ISending
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final MqttTopic	mqttTopic;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param topic
	 * @param host
	 * @param port
	 * @throws MqttException
	 */
	public SimpleSender(ETopics topic, String host, int port) throws MqttException
	{
		super();
		setHostPort(host, port);
		connect();
		mqttTopic = getMqtt().getTopic(topic.getName());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean publish(ETopics topic, byte[] message, IMessageSender sender)
	{
		if (isConnected())
		{
			
			final MqttMessage mqttMessage = new MqttMessage(message);
			mqttMessage.setQos(topic.getType().getQos());
			mqttMessage.setRetained(topic.getType().isRetained());
			final SendMessage sendMessage = new SendMessage(mqttTopic, mqttMessage);
			
			new Thread(sendMessage).start();
			// mqttTopic.publish(mqttMessage);
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	public class SendMessage implements Runnable
	{
		
		// --------------------------------------------------------------------------
		// --- variables and constants ----------------------------------------------
		// --------------------------------------------------------------------------
		private final MqttMessage	message;
		private final MqttTopic		topic;
		
		
		// --------------------------------------------------------------------------
		// --- constructors ---------------------------------------------------------
		// --------------------------------------------------------------------------
		/**
		 * @param topic
		 * @param message
		 */
		public SendMessage(MqttTopic topic, MqttMessage message)
		{
			this.topic = topic;
			this.message = message;
		}
		
		
		// --------------------------------------------------------------------------
		// --- methods --------------------------------------------------------------
		// --------------------------------------------------------------------------
		@Override
		public void run()
		{
			try
			{
				topic.publish(message);
			} catch (MqttPersistenceException err)
			{
				err.printStackTrace();
			} catch (MqttException err)
			{
				err.printStackTrace();
			}
		}
	}
	
	
	@Override
	public boolean publish(ETopics topic, byte[] message)
	{
		return this.publish(topic, message, null);
	}
}
