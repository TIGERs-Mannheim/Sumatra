/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication;

/**
 * Communication statistics packet.
 * 
 * @author AndreR
 * 
 */
public class Statistics implements Cloneable
{
	public int payload = 0;
	public int raw = 0;
	public int packets = 0;
	private long lastReset;
	
	public Statistics()
	{
		lastReset = System.nanoTime();
	}
	
	public void reset()
	{
		payload = 0;
		raw = 0;
		packets = 0;
		
		lastReset = System.nanoTime();
	}
	
	public long getLastResetTimestamp()
	{
		return lastReset;
	}
	
	public Statistics substract(Statistics stat)
	{
		Statistics ret = new Statistics();
		ret.payload = payload - stat.payload;
		ret.raw = raw - stat.raw;
		ret.packets = packets - stat.packets;
		
		return ret;
	}
	
	public Statistics add(Statistics stat)
	{
		Statistics ret = new Statistics();
		ret.payload = payload + stat.payload;
		ret.raw = raw + stat.raw;
		ret.packets = packets + stat.packets;
		
		return ret;
	}
	
	public float getOverheadPercentage()
	{
		if(raw == 0)
		{
			return 0;
		}
		
		return (1.0f - ((float)payload)/((float)raw));
	}
	
	public float getLoadPercentage(float passedTime)
	{
		if(passedTime == 0)
		{
			return 0;
		}
		
		return (((float)raw)/(28800.0f*passedTime));
	}
	
	public float getLoadPercentageWithLastReset()
	{
		return getLoadPercentage(((float)(System.nanoTime() - lastReset))/1000000000.0f);
	}
	
	public Statistics clone()
	{
		Statistics ret = new Statistics();
		
		ret.payload = payload;
		ret.raw = raw;
		ret.packets = packets;
		ret.lastReset = lastReset;
		
		return ret;
	}
}
