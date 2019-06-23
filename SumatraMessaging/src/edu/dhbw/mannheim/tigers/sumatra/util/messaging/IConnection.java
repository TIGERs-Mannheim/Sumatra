/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

import org.eclipse.paho.client.mqttv3.MqttException;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IConnection
{
	/**
	 * @return
	 */
	boolean connect();
	
	
	/**
	 * @return
	 */
	boolean disconnect();
	
	
	/**
	 * @return
	 */
	boolean isConnected();
	
	
	/**
	 * @param host
	 * @param port
	 * @throws MqttException
	 */
	void setHostPort(String host, int port) throws MqttException;
}
