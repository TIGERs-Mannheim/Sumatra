/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.connection;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.Messaging;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IReceiving;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.IMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.ISending;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.TestProtos;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ConnectionTest
{
	
	/**
	 * Tests if connection works after disconnect and reconnect
	 */
	@Test
	@Ignore
	public void testConnectDisconnectPublish()
	{
		IReceiving rcv = Messaging.getDefaultCon();
		// IReceiving rcv = new SimpleReceiver();
		rcv.connect();
		ConnectionSenderReceiver sndRcv = new ConnectionSenderReceiver();
		rcv.addMessageReceiver(ETopics.TEST_RECONNECT_PUBLISH, sndRcv);
		
		ISending snd = Messaging.getDefaultCon();
		// ISending snd = new SimpleSender(ETopics.TEST_RECONNECT_PUBLISH);
		
		
		publish(snd, sndRcv, 0, true);
		try
		{
			Thread.sleep(100);
		} catch (InterruptedException err)
		{
			err.printStackTrace();
		}
		snd.disconnect();
		try
		{
			Thread.sleep(10000);
		} catch (InterruptedException err)
		{
			err.printStackTrace();
		}
		publish(snd, sndRcv, 1, false);
		snd.connect();
		
		publish(snd, sndRcv, 2, false);
		try
		{
			Thread.sleep(100);
		} catch (InterruptedException err)
		{
			err.printStackTrace();
		}
		snd.disconnect();
		
		System.out.println("0:" + sndRcv.isMessageArrived(0) + " 1:" + sndRcv.isMessageArrived(1) + " 2:"
				+ sndRcv.isMessageArrived(2));
		
		assertTrue("ID 0 message not arrived", sndRcv.isMessageArrived(0));
		assertTrue("ID 1 message not arrived", sndRcv.isMessageArrived(1));
		assertTrue("ID 2 message not arrived", sndRcv.isMessageArrived(2));
	}
	
	
	private void publish(ISending con, IMessageSender sndRcv, int id, boolean first)
	{
		TestProtos.ConnectionInfo.Builder builder = TestProtos.ConnectionInfo.newBuilder();
		builder.setId(id);
		builder.setFirstConnection(first);
		con.publish(ETopics.TEST_RECONNECT_PUBLISH, builder.build().toByteArray(), sndRcv);
	}
}
