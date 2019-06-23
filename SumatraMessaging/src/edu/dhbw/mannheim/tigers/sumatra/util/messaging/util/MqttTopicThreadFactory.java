/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.util;

import java.util.concurrent.ThreadFactory;


/**
 * Factory for threads
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class MqttTopicThreadFactory implements ThreadFactory
{
	private String	name;
	
	
	/**
	 * @param name
	 */
	public MqttTopicThreadFactory(String name)
	{
		this.name = name;
	}
	
	
	@Override
	public Thread newThread(Runnable r)
	{
		return new Thread(r, name);
	}
}
