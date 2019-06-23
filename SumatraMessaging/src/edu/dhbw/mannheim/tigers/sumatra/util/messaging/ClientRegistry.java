/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * This class counts how many clients are registerd to a module.
 * TODO not yet finished
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class ClientRegistry implements IMessageReceivable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private Map<ETopics, Set<String>>	registry;
	private Map<ETopics, Integer>			counters;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param con
	 * 
	 */
	public ClientRegistry(MessageConnection con)
	{
		registry = new HashMap<ETopics, Set<String>>();
		counters = new HashMap<ETopics, Integer>();
		
		con.addMessageReceiver(ETopics.REGISTRY, this);
		con.addMessageReceiver(ETopics.AI_REGISTRY, this);
		con.addMessageReceiver(ETopics.MODULI_REGISTRY, this);
		con.addMessageReceiver(ETopics.APOLLON_REGISTRY, this);
		con.addMessageReceiver(ETopics.LACHESIS_REGISTRY, this);
		con.addMessageReceiver(ETopics.ATHENA_REGISTRY, this);
		con.addMessageReceiver(ETopics.LOG_REGISTRY, this);
		con.addMessageReceiver(ETopics.TIMER_REGISTRY, this);
		con.addMessageReceiver(ETopics.EMERGENCY_REGISTRY, this);
		con.addMessageReceiver(ETopics.REFEREE_REGISTRY, this);
		con.addMessageReceiver(ETopics.RCM_REGISTRY, this);
		con.addMessageReceiver(ETopics.WF_REGISTRY, this);
		con.addMessageReceiver(ETopics.TEST_REGISTRY, this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onConnectionEvent(boolean connected)
	{
		
	}
	
	
	@Override
	public void messageArrived(ETopics mqttTopic, Message message)
	{
		if (message instanceof RegistryProtos.Client)
		{
			RegistryProtos.Client msg = (RegistryProtos.Client) message;
			String clientId = msg.getClientId();
			switch (mqttTopic)
			{
				case REGISTRY:
					removeClient(clientId);
					break;
				/**
				 * There should only be registry topics.
				 */
				default:
					addClient(mqttTopic, clientId);
					break;
			}
		}
	}
	
	
	/**
	 * 
	 * @param topic
	 * @param clientId
	 */
	private void addClient(ETopics topic, String clientId)
	{
		Set<String> clients = registry.get(topic);
		if (clients == null)
		{
			clients = new HashSet<String>();
		}
		clients.add(clientId);
		int numOfClients = clients.size();
		counters.remove(topic);
		counters.put(topic, numOfClients);
	}
	
	
	/**
	 * 
	 * @param clientId
	 */
	private void removeClient(String clientId)
	{
		for (Set<String> clients : registry.values())
		{
			if (clients != null)
			{
				clients.remove(clientId);
			}
		}
		updateCounters();
	}
	
	
	/**
	 * 
	 */
	private void updateCounters()
	{
		for (Entry<ETopics, Set<String>> entry : registry.entrySet())
		{
			if (entry != null)
			{
				int numOfClients = entry.getValue().size();
				counters.remove(entry.getKey());
				counters.put(entry.getKey(), numOfClients);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Returns the number of registered clients for the given topic module
	 * @param topic
	 * @return
	 */
	public int getCounter(ETopics topic)
	{
		Integer counter = counters.get(topic);
		if (counter == null)
		{
			counter = 0;
		}
		return counter;
	}
}
