/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.latency;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.TestProtos;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class LatencyReceiver extends AStats implements IMessageReceivable
{
	private int	lastId;
	
	
	/**
	 * @param numOfMessages
	 */
	public LatencyReceiver(int numOfMessages)
	{
		super(numOfMessages);
	}
	
	
	@Override
	public void onConnectionEvent(boolean connected)
	{
	}
	
	
	@Override
	public void messageArrived(ETopics mqttTopic, Message message)
	{
		switch (mqttTopic)
		{
			case TEST_LATENCY_LOCAL:
				if (message instanceof TestProtos.TimerInfo)
				{
					TestProtos.TimerInfo msg = (TestProtos.TimerInfo) message;
					long end = System.nanoTime();
					long latency = end - msg.getStart();
					putValue(msg.getId(), latency);
					lastId = msg.getId();
				}
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * last received ID
	 * @return
	 */
	public int getLastId()
	{
		return lastId;
	}
}
