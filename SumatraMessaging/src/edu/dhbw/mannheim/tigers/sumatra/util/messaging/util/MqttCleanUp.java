/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 21, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.util;

import java.lang.reflect.Field;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageConnection;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.Messaging;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.IMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * This class should be used to clean all retained messages from the mqtt broker.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class MqttCleanUp implements IMessageSender
{
	private static final long		WAIT_FOR_COMPLETITION_TIME	= 1000;
	private static final int		PORT								= 1883;
	private static final String	HOST								= "localhost";
	
	
	/**
	 * Cleans all messages via sending empty messages
	 * @param host
	 * @param port
	 * @throws NoSuchFieldException
	 */
	public void cleanMessages(String host, int port) throws NoSuchFieldException
	{
		MessageConnection msgObs = Messaging.getDefaultCon();
		msgObs.setConnectionInfo(host, port);
		msgObs.connect();
		byte[] emptyBytes = {};
		Field typeField = ETopics.class.getDeclaredField("type");
		typeField.setAccessible(true);
		for (ETopics topic : ETopics.values())
		{
			String message = "not cleared";
			try
			{
				typeField.set(topic, MessageType.CONTROL);
				msgObs.publish(topic, emptyBytes, this);
				message = "cleared";
			} catch (IllegalArgumentException err)
			{
				System.err.println(err.getMessage());
			} catch (IllegalAccessException err)
			{
				System.err.println(err.getMessage());
			}
			System.out.println(topic.name() + " " + message + ", topic: " + topic.getName());
		}
	}
	
	
	@Override
	public void deliveryComplete(MqttMessage message)
	{
		// nothing to do
	}
	
	
	@Override
	public void deliveryComplete()
	{
		// nothing to do
	}
	
	
	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws NoSuchFieldException
	 */
	public static void main(String[] args) throws InterruptedException, NoSuchFieldException
	{
		new MqttCleanUp().cleanMessages(HOST, PORT);
		Thread.sleep(WAIT_FOR_COMPLETITION_TIME);
		System.exit(0);
	}
	
	
	@Override
	public void onConnectionEvent(boolean connected)
	{
	}
}
