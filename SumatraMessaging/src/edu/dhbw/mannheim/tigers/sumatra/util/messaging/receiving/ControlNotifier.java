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
import java.util.concurrent.Future;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * Notifies about control information. Each message should be processed.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class ControlNotifier extends AMessageNotifier
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** Hold last message for subscribers that arrived later the mesasage is received */
	private Message	lastMessage	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param mqttTopic
	 */
	public ControlNotifier(ETopics mqttTopic)
	{
		super(mqttTopic, "Control-" + mqttTopic.toString());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * The new message will be submitted.
	 */
	@Override
	public void notify(Message message)
	{
		lastMessage = message;
		NotifyMessage nMsg = new NotifyMessage(getMqttTopic(), message, getReceivers());
		List<Future<Boolean>> threadResults = getThreadResults();
		
		// Keep the queue list short
		if (!threadResults.isEmpty())
		{
			for (Future<Boolean> next : threadResults)
			{
				if (next.isDone())
				{
					threadResults.remove(next);
				} else
				{
					break;
				}
			}
		}
		
		// submit new message
		getExecutor().submit(nMsg);
	}
	
	
	/**
	 * Calls super method and notifies the new receiver about the last message, if one exists.
	 */
	@Override
	public void addMessageReceiver(IMessageReceivable o)
	{
		super.addMessageReceiver(o);
		if (lastMessage != null)
		{
			o.messageArrived(getMqttTopic(), lastMessage);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
