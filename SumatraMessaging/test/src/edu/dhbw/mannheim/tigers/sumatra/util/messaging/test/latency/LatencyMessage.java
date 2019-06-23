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

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.ISending;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.TestProtos;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class LatencyMessage
{
	
	
	private long		start	= 0;
	
	
	private ISending	con;
	private int			id;
	private long		latency;
	
	
	/**
	 * @param id
	 * @param sSnd
	 * 
	 */
	public LatencyMessage(int id, ISending sSnd)
	{
		this.id = id;
		con = sSnd;
	}
	
	
	/**
	 * 
	 */
	public void timeMeasure()
	{
		start = System.nanoTime();
		TestProtos.TimerInfo.Builder builder = TestProtos.TimerInfo.newBuilder();
		builder.setId(id);
		builder.setStart(start);
		con.publish(ETopics.TEST_LATENCY_LOCAL, builder.build().toByteArray());
	}
	
	
	@Override
	public String toString()
	{
		return "Message " + id + ":  latency " + (getLatency());
	}
	
	
	/**
	 * 
	 * @return
	 */
	public long getLatency()
	{
		return latency;
	}
	
	
	/**
	 * @return the start
	 */
	public final long getStart()
	{
		return start;
	}
}
