/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * ****************************************************MAX_MESSAGES / 10*****
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IReceiving;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.Receiving;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.IMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.ISending;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.Sending;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ITopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class MessageConnection implements ISending, IReceiving
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static Logger								log	= Logger.getLogger(MessageConnection.class.getName());
	
	
	private final Callbacks<IReceiving>				receiveCallbacks;
	private final Callbacks<ISending>				sendCallbacks;
	
	private final Set<IConnectionStateObserver>	conObservers;
	
	private String											host;
	private int												port;
	
	
	private final ClientRegistry						registry;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	protected MessageConnection()
	{
		log.debug("New MessageConnection");
		
		receiveCallbacks = new Callbacks<IReceiving>(new Receiving(MessageType.CONTROL), new Receiving(
				MessageType.DURABLE_INFO), new Receiving(MessageType.NONDURABLE_INFO));
		sendCallbacks = new Callbacks<ISending>(new Sending(MessageType.CONTROL), new Sending(MessageType.DURABLE_INFO),
				new Sending(MessageType.NONDURABLE_INFO));
		
		conObservers = new CopyOnWriteArraySet<IConnectionStateObserver>();
		
		registry = new ClientRegistry(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param topic
	 * @param msgRcv
	 */
	@Override
	public void addMessageReceiver(ETopics topic, IMessageReceivable msgRcv)
	{
		MessageType type = topic.getType();
		IReceiving rcv = receiveCallbacks.get(type);
		rcv.addMessageReceiver(topic, msgRcv);
	}
	
	
	/**
	 * @param topic
	 * @param msgRcv
	 */
	@Override
	public void removeMessageReceiver(ETopics topic, IMessageReceivable msgRcv)
	{
		MessageType type = topic.getType();
		IReceiving rcv = receiveCallbacks.get(type);
		rcv.removeMessageReceiver(topic, msgRcv);
	}
	
	
	/**
	 * @param o
	 */
	public void addConnectionObserver(IConnectionStateObserver o)
	{
		conObservers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeConnectionObserver(IConnectionStateObserver o)
	{
		conObservers.remove(o);
	}
	
	
	/**
	 * 
	 * TODO Daniel Andres put this notify in Sender class like the notifier in Receiving
	 * 
	 * @param connected
	 */
	private void notifyConnectionEvent(boolean connected)
	{
		for (IConnectionStateObserver o : conObservers)
		{
			o.onConnectionEvent(connected);
		}
	}
	
	
	/**
	 * 
	 * @param topic
	 * @return
	 */
	private ISending getSender(ITopics topic)
	{
		return sendCallbacks.get(topic.getType());
	}
	
	
	/**
	 * Sends a message on a specific topic. Adds the sender as delivery callback
	 * @param topic
	 * @param message
	 * @param sender
	 * @return
	 */
	@Override
	public boolean publish(ETopics topic, byte[] message, IMessageSender sender)
	{
		return getSender(topic).publish(topic, message, sender);
	}
	
	
	/**
	 * Sends a message on a specific topic. Adds no delivery callback
	 * @param topic
	 * @param message
	 * @return
	 */
	@Override
	public boolean publish(ETopics topic, byte[] message)
	{
		return getSender(topic).publish(topic, message, null);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	@Override
	public boolean connect()
	{
		boolean allConnected = true;
		for (MessageType type : MessageType.values())
		{
			if (!receiveCallbacks.get(type).connect())
			{
				allConnected = false;
			}
			if (!sendCallbacks.get(type).connect())
			{
				allConnected = false;
			}
		}
		notifyConnectionEvent(allConnected);
		return allConnected;
	}
	
	
	/**
	 * 
	 * @return
	 */
	@Override
	public boolean disconnect()
	{
		boolean allDisconnected = true;
		for (MessageType type : MessageType.values())
		{
			if (!receiveCallbacks.get(type).disconnect())
			{
				allDisconnected = false;
			}
			if (!sendCallbacks.get(type).disconnect())
			{
				allDisconnected = false;
			}
		}
		return allDisconnected;
	}
	
	
	/**
	 * Checks if a sender is connected
	 * 
	 * @param type
	 * @return
	 */
	public boolean isSendConnected(MessageType type)
	{
		return sendCallbacks.get(type).isConnected();
	}
	
	
	/**
	 * Checks if a receiver is connected
	 * @param type
	 * @return
	 */
	public boolean isRcvConnected(MessageType type)
	{
		return receiveCallbacks.get(type).isConnected();
	}
	
	
	/**
	 * 
	 * @param host
	 * @param port
	 */
	public void setConnectionInfo(String host, int port)
	{
		log.debug("messaging host: " + host + "; port: " + port);
		this.host = host;
		this.port = port;
		
		for (MessageType type : MessageType.values())
		{
			try
			{
				receiveCallbacks.get(type).setHostPort(host, port);
			} catch (MqttException err)
			{
				log.error("MqttException on Receiver of type " + type.toString(), err);
			}
			try
			{
				sendCallbacks.get(type).setHostPort(host, port);
			} catch (MqttException err)
			{
				log.error("MqttException on Sender of type " + type.toString(), err);
			}
		}
		
	}
	
	
	/**
	 * 
	 * @param hostAndPort host and Port seperated by : (colon) or only the host
	 */
	public void setConnectionInfo(String hostAndPort)
	{
		String[] param = hostAndPort.split(":");
		String newHost = param[0];
		int newPort;
		if (param.length >= 2)
		{
			newPort = Integer.valueOf(param[1]);
		} else
		{
			newPort = Messaging.DEFAULT_PORT;
		}
		setConnectionInfo(newHost, newPort);
	}
	
	
	@Override
	public String toString()
	{
		return host + ":" + port;
	}
	
	
	/**
	 * TODO Daniel implement isConnected
	 */
	@Override
	public boolean isConnected()
	{
		return false;
	}
	
	
	@Override
	public void setHostPort(String host, int port)
	{
		setConnectionInfo(host, port);
	}
	
	
	/**
	 * Returns the number of registered clients for the given topic module
	 * @param topic
	 * @return
	 */
	public int getCounter(ETopics topic)
	{
		return registry.getCounter(topic);
	}
	
	/**
	 * This class hold the callbacks for sending or receiving for each messagetype. This apporach is used as it is faster
	 * then a map in this case of a small set.
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private static class Callbacks<C extends IConnection>
	{
		private final C	control;
		private final C	durable;
		private final C	nonDurable;
		
		
		/**
		 * @param control
		 * @param durable
		 * @param nonDurable
		 */
		protected Callbacks(C control, C durable, C nonDurable)
		{
			this.control = control;
			this.durable = durable;
			this.nonDurable = nonDurable;
		}
		
		
		/**
		 * @param type
		 * @return
		 */
		C get(MessageType type)
		{
			switch (type)
			{
				case CONTROL:
					return control;
				case DURABLE_INFO:
					return durable;
				case NONDURABLE_INFO:
					return nonDurable;
				default:
					// Fallback nonDurable Callback
					return nonDurable;
			}
		}
	}
}
