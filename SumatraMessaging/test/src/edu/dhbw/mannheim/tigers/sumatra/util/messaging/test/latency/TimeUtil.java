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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class TimeUtil
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		for (int i = 0; i < 10; i++)
		{
			long time = System.nanoTime();
			Date date = new Date(TimeUnit.NANOSECONDS.toMillis(time));
			double seconds = time / 1000000000.0;
			System.out.println(seconds);
			String result = new SimpleDateFormat("HH:mm:ss:SSS").format(date);
			System.out.println(result);
			System.out.println("");
		}
		
	}
}
