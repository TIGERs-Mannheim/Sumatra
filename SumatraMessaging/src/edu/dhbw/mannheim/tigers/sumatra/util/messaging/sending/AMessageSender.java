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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public abstract class AMessageSender
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private MqttTopic												mqttTopic;
	
	private ExecutorService										executor	= null;
	private volatile List<Future<MqttDeliveryToken>>	threadResults;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  */
	public AMessageSender()
	{
		executor = Executors.newSingleThreadScheduledExecutor();
		threadResults = new CopyOnWriteArrayList<Future<MqttDeliveryToken>>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the mqttTopic
	 */
	public MqttTopic getTopic()
	{
		return mqttTopic;
	}
	
	
	/**
	 * @param mqttTopic the mqttTopic to set
	 */
	public void setTopic(MqttTopic mqttTopic)
	{
		this.mqttTopic = mqttTopic;
	}
	
	
	/**
	 * @param mqttMessage
	 * @param tokens
	 * @param sender
	 */
	public void publish(MqttMessage mqttMessage, Map<MqttDeliveryToken, IMessageSender> tokens, IMessageSender sender)
	{
		final SendMessage sendMessage = new SendMessage(mqttTopic, mqttMessage, tokens, sender);
		
		final Future<MqttDeliveryToken> futureToken = executor.submit(sendMessage);
		threadResults.add(futureToken);
	}
	
	
	/**
	 * @return the threadResults
	 */
	public List<Future<MqttDeliveryToken>> getThreadResults()
	{
		return threadResults;
	}
}
