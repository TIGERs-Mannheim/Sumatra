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
 * Notifies about information. Each message should be processed.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class InfoStatefullNotifier extends AMessageNotifier
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param mqttTopic
	 */
	public InfoStatefullNotifier(ETopics mqttTopic)
	{
		super(mqttTopic, "Statefull-" + mqttTopic.toString());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Checks if there are other messages in the executor queue. The notify of the first message could finish. All others
	 * will be cancelled. The new message is then submitted.
	 */
	@Override
	public void notify(Message message)
	{
		NotifyMessage nMsg = new NotifyMessage(getMqttTopic(), message, getReceivers());
		List<Future<Boolean>> threadResults = getThreadResults();
		if (!threadResults.isEmpty())
		{
			Future<Boolean> first = threadResults.get(0);
			// remove the first message from when list, when still in execution, so prevents from cancel
			if (!first.isDone())
			{
				threadResults.remove(0);
			}
			// cancel all events in list
			for (Future<Boolean> next : threadResults)
			{
				next.cancel(true);
			}
			threadResults.clear();
		}
		getExecutor().submit(nMsg);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
