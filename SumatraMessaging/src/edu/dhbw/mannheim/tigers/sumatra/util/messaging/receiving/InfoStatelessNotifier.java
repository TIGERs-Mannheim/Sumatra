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
 * Notifies about information. Only newest message should be processed
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class InfoStatelessNotifier extends AMessageNotifier
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
	public InfoStatelessNotifier(ETopics mqttTopic)
	{
		super(mqttTopic, "Stateless-" + mqttTopic.toString());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Checks if there are other messages in the executor queue. All prior notifies
	 * will be cancelled. The new message is then submitted.
	 */
	@Override
	public void notify(Message message)
	{
		NotifyMessage nMsg = new NotifyMessage(getMqttTopic(), message, getReceivers());
		List<Future<Boolean>> threadResults = getThreadResults();
		if (!threadResults.isEmpty())
		{
			// cancel all events
			for (Future<Boolean> next : threadResults)
			{
				next.cancel(true);
			}
			threadResults.clear();
		}
		
		// submit new message
		threadResults.add(getExecutor().submit(nMsg));
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
