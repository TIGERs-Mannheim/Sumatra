/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.AMessageNotifier;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.ControlNotifier;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.InfoStatefullNotifier;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.InfoStatelessNotifier;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.AMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.ControlSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.InfoStatefullSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.InfoStatelessSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * The MessageType defines the QoS of the delivery of the MQTT messages.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public enum MessageType
{
	/**  */
	CONTROL(2, true),
	/**  */
	DURABLE_INFO(1, false),
	/**  */
	NONDURABLE_INFO(0, false);
	
	private final int			qos;
	private final boolean	retained;
	
	
	private MessageType(int qos, boolean retained)
	{
		this.qos = qos;
		this.retained = retained;
	}
	
	
	/**
	 * @param mqttTopic
	 * @return
	 */
	public AMessageNotifier getMessageNotifier(ETopics mqttTopic)
	{
		switch (this)
		{
			case DURABLE_INFO:
				return new InfoStatefullNotifier(mqttTopic);
			case NONDURABLE_INFO:
				return new InfoStatelessNotifier(mqttTopic);
			case CONTROL:
			default:
				return new ControlNotifier(mqttTopic);
		}
	}
	
	
	/**
	 * @return
	 */
	public AMessageSender getMessageSender()
	{
		switch (this)
		{
			case DURABLE_INFO:
				return new InfoStatefullSender();
			case NONDURABLE_INFO:
				return new InfoStatelessSender();
			case CONTROL:
			default:
				return new ControlSender();
		}
	}
	
	
	/**
	 * @return the qos
	 */
	public int getQos()
	{
		return qos;
	}
	
	
	/**
	 * @return the retained
	 */
	public boolean isRetained()
	{
		return retained;
	}
}
