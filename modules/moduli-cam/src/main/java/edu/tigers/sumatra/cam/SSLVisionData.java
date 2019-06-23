/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2011
 * Author(s): Yakisoba
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Container class for SSLVision data.
 * 
 * @author Yakisoba
 */
public class SSLVisionData implements Serializable
{
	private static final long	serialVersionUID	= -1885492267591611080L;
	
	private final long			timestamp;
	private final byte[]			data;
	
	
	/**
	 * @param timestamp
	 * @param data
	 */
	public SSLVisionData(final long timestamp, final byte[] data)
	{
		this.timestamp = timestamp;
		this.data = Arrays.copyOf(data, data.length);
	}
	
	
	/**
	 * @return
	 */
	public byte[] getData()
	{
		return Arrays.copyOf(data, data.length);
	}
	
	
	/**
	 * @return
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
}
