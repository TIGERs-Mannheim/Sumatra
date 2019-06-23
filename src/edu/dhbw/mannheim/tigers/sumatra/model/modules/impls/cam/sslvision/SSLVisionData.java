/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2011
 * Author(s): Yakisoba
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision;

import java.io.Serializable;

/**
 * TODO Yakisoba, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Yakisoba
 * 
 */
public class SSLVisionData implements Serializable
{
	private static final long	serialVersionUID	= -1885492267591611080L;
	
	private long timestamp;
	private byte[] data;
	
	public SSLVisionData(long timestamp, byte[] data) {
		this.timestamp = timestamp;
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
