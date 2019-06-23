/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.IConnectionStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.util.MqttTopicThreadFactory;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public abstract class AMessageNotifier
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log		= Logger.getLogger(AMessageNotifier.class.getName());
	
	private final ETopics						mqttTopic;
	private final List<IMessageReceivable>	receivers;
	
	private ExecutorService						executor	= null;
	
	
	private volatile List<Future<Boolean>>	threadResults;
	private final String							name;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param mqttTopic
	 * @param name
	 */
	public AMessageNotifier(ETopics mqttTopic, String name)
	{
		this.mqttTopic = mqttTopic;
		this.name = name;
		receivers = new LinkedList<IMessageReceivable>();
		open();
		threadResults = new CopyOnWriteArrayList<Future<Boolean>>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addMessageReceiver(IMessageReceivable o)
	{
		getReceivers().add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeMessageReceiver(IMessageReceivable o)
	{
		getReceivers().remove(o);
	}
	
	
	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return !getReceivers().isEmpty();
	}
	
	
	/**
	 * Prepares this notifiers for service.
	 */
	public final void open()
	{
		setExecutor(Executors.newSingleThreadScheduledExecutor(new MqttTopicThreadFactory(name)));
	}
	
	
	/**
	 * closes this notifiers service
	 */
	public final void close()
	{
		getExecutor().shutdown();
	}
	
	
	/**
	 * @param message
	 */
	public abstract void notify(Message message);
	
	
	/**
	 * @return the executor
	 */
	public ExecutorService getExecutor()
	{
		return executor;
	}
	
	
	/**
	 * @param executor the executor to set
	 */
	public void setExecutor(ExecutorService executor)
	{
		this.executor = executor;
	}
	
	
	/**
	 * @return the mqttTopic
	 */
	public ETopics getMqttTopic()
	{
		return mqttTopic;
	}
	
	
	/**
	 * @return the receivers
	 */
	public List<IMessageReceivable> getReceivers()
	{
		return receivers;
	}
	
	
	/**
	 * @return the threadResults
	 */
	protected List<Future<Boolean>> getThreadResults()
	{
		return threadResults;
	}
	
	
	/**
	 * 
	 * @param mqttMessage
	 */
	public void messageArrived(MqttMessage mqttMessage)
	{
		try
		{
			Builder<?> builder = (Builder<?>) mqttTopic.getProtoType();
			Message message = builder.mergeFrom(mqttMessage.getPayload()).build();
			this.notify(message);
			
		} catch (InvalidProtocolBufferException e)
		{
			log.error(e.getMessage(), e);
		}
	}
	
	
	protected void onConnectionEvent(boolean connected)
	{
		for (IConnectionStateObserver o : receivers)
		{
			o.onConnectionEvent(connected);
		}
	}
}
