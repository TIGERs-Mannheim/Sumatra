/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 19, 2012
 * Author(s): andres
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.util.MqttIdGenerator;


/**
 * This is the class that can receive messages from an subscribed MQTT topic.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public abstract class ACallback implements MqttCallback
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log					= Logger.getLogger(ACallback.class.getName());
	
	private boolean					connected			= false;
	private MqttClient				mqtt					= null;
	
	private String						clientId;
	private static final int		RECONNECT_SLEEP	= 5000;
	
	private DisconnectHook			hook					= null;
	
	private Set<String>				subscriptions;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	protected ACallback()
	{
		clientId = MqttIdGenerator.newId();
		subscriptions = new HashSet<String>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	public boolean connect()
	{
		try
		{
			return connectMqtt();
		} catch (MqttSecurityException err)
		{
			String cause = "";
			if (err.getReasonCode() == 0)
			{
				if (err.getCause() != null)
				{
					cause = " ;" + err.getCause().getMessage() + ": " + err.getCause().getClass().getName();
				}
			}
			log.error("Can't connect to " + mqtt.getServerURI() + "; RC: " + err.getReasonCode() + cause);
			return false;
		} catch (MqttException err)
		{
			String cause = "";
			if (err.getReasonCode() == 0)
			{
				if (err.getCause() != null)
				{
					cause = " ;" + err.getCause().getMessage() + ": " + err.getCause().getClass().getName();
				}
			}
			log.error("Can't connect to " + mqtt.getServerURI() + "; RC: " + err.getReasonCode() + cause);
			return false;
		}
	}
	
	
	/**
	 * @return
	 * @throws MqttException
	 * @throws MqttSecurityException
	 */
	protected boolean connectMqtt() throws MqttException
	{
		if (getMqtt() == null)
		{
			setHostPort(Messaging.DEFAULT_HOST, Messaging.DEFAULT_PORT);
		}
		if (getMqtt() != null)
		{
			getMqtt().setCallback(this);
			getMqtt().connect();
			connected = true;
			resubscribe();
		} else
		{
			connected = false;
		}
		return connected;
	}
	
	
	/**
	 * Disconnects this Callback.
	 * @return
	 */
	public boolean disconnect()
	{
		try
		{
			disconnectMqtt();
			return true;
		} catch (MqttException err)
		{
			log.error("Can't disconnect from " + mqtt.getServerURI() + "; RC: " + err.getReasonCode());
			return false;
		}
	}
	
	
	/**
	 * Closes the mqtt connection and removes the corresponding disconnect hook from the runtime.
	 * @throws MqttException
	 */
	private void disconnectMqtt() throws MqttException
	{
		if ((mqtt != null) && connected)
		{
			mqtt.disconnect(0);
			connected = false;
		}
	}
	
	
	private void closeMqtt()
	{
		if (hook != null)
		{
			hook.remove();
			hook = null;
		}
		mqtt = null;
	}
	
	
	/**
	 * Subscribes a topic on the broker
	 * @param topic
	 * @param qos
	 */
	public void subscribe(String topic, int qos)
	{
		subscriptions.add(topic);
		if (connected)
		{
			try
			{
				mqtt.subscribe(topic, qos);
				log.debug("Subscribed to " + topic);
			} catch (final MqttSecurityException e)
			{
			} catch (final MqttException e)
			{
			}
		}
	}
	
	
	/**
	 * Unsubscribes the topic from the broker.
	 * @param topic
	 */
	public void unsubscribe(String topic)
	{
		try
		{
			subscriptions.remove(topic);
			if (connected)
			{
				mqtt.unsubscribe(topic);
			}
		} catch (final MqttException e)
		{
			log.warn("MqttException: " + e.getMessage());
		}
	}
	
	
	/**
	 * Resuscribe to all currently used topics.
	 */
	private void resubscribe()
	{
		if (connected)
		{
			for (String topic : subscriptions)
			{
				try
				{
					mqtt.subscribe(topic, 0);
				} catch (final MqttSecurityException e)
				{
				} catch (final MqttException e)
				{
				}
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the mqtt
	 */
	protected final MqttClient getMqtt()
	{
		return mqtt;
	}
	
	
	/**
	 * @return
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	
	/**
	 * Setter for Host and Port. Creates a new Mqtt Client based on parameters, adds a new Shutdown hook and removed
	 * the old one
	 * 
	 * @param host
	 * @param port
	 * @throws MqttException thrown when the connection couldn't be established
	 */
	public void setHostPort(String host, int port) throws MqttException
	{
		closeMqtt();
		
		String tmpDir = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + clientId;
		MqttDefaultFilePersistence mqttPersistenceStore = new MqttDefaultFilePersistence(tmpDir);
		
		mqtt = new MqttClient("tcp://" + host + ":" + port, clientId, mqttPersistenceStore);
		
		hook = new DisconnectHook(mqtt);
		hook.add();
	}
	
	
	@Override
	public void connectionLost(Throwable cause)
	{
		// this indicates if the connection is successfully reconnected
		boolean reconnected = false;
		
		synchronized (this)
		{
			while (connected && !reconnected)
			{
				log.error("Connection lost " + mqtt.getServerURI());
				try
				{
					this.wait(RECONNECT_SLEEP);
				} catch (final InterruptedException e)
				{
					// nothing to do
				}
				if (checkNetwork())
				{
					try
					{
						connectMqtt();
						reconnected = true;
					} catch (MqttException err)
					{
						// nothing to do, because it will be tried again
					}
				}
			}
			resubscribe();
		}
		log.info("Connection reestablished");
	}
	
	
	@Override
	public void messageArrived(String topic, MqttMessage message)
	{
		// pre implemented without functionallity. The concrete class that want functionalliyt has to override this
	}
	
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		// pre implemented without functionallity. The concrete class that want functionalliyt has to override this
	}
	
	
	private boolean checkNetwork()
	{
		try
		{
			final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (final NetworkInterface netint : Collections.list(nets))
			{
				final Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (final InetAddress inetAddress : Collections.list(inetAddresses))
				{
					log.info("Found one network address:" + inetAddress.getHostAddress());
					return true;
				}
			}
		} catch (final SocketException e)
		{
			return false;
		}
		return false;
	}
	
	/**
	 * Disconnect Hook for Mqtt. If connection is not closed properly
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private static class DisconnectHook extends Thread
	{
		private MqttClient	mqtt;
		
		
		/**
		 * @param mqtt
		 */
		public DisconnectHook(MqttClient mqtt)
		{
			this.mqtt = mqtt;
		}
		
		
		/**
		 * Removes this hook from Runtime
		 */
		public void remove()
		{
			Runtime.getRuntime().removeShutdownHook(this);
		}
		
		
		/**
		 * Adds this hook to Runtime
		 */
		public void add()
		{
			Runtime.getRuntime().addShutdownHook(this);
		}
		
		
		@Override
		public void run()
		{
			if ((mqtt != null) && mqtt.isConnected())
			{
				try
				{
					mqtt.disconnect();
				} catch (MqttException e)
				{
					log.error(e.getMessage());
				}
			}
		}
	}
}
