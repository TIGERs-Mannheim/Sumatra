/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving;

import java.util.List;
import java.util.concurrent.Callable;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * This class notifies a set of receivers for a specific topic about an arrived message
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class NotifyMessage implements Callable<Boolean>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ETopics						mqttTopic;
	private final Message						message;
	private final List<IMessageReceivable>	receivers;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	protected NotifyMessage(ETopics mqttTopic, Message message, List<IMessageReceivable> receivers)
	{
		this.mqttTopic = mqttTopic;
		this.message = message;
		this.receivers = receivers;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Boolean call()
	{
		for (final IMessageReceivable messageReceiver : this.receivers)
		{
			messageReceiver.messageArrived(this.mqttTopic, this.message);
		}
		return true;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
