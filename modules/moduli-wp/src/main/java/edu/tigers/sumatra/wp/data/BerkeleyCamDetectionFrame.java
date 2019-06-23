/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


/**
 * Entity for vision data
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Entity
public class BerkeleyCamDetectionFrame
{
	@PrimaryKey
	private final long timestamp;
	
	private final ExtendedCamDetectionFrame camFrame;
	
	
	@SuppressWarnings("unused")
	private BerkeleyCamDetectionFrame()
	{
		timestamp = 0;
		camFrame = null;
	}
	
	
	/**
	 * @param camFrame to be saved
	 */
	public BerkeleyCamDetectionFrame(final ExtendedCamDetectionFrame camFrame)
	{
		this.camFrame = camFrame;
		timestamp = camFrame.gettCapture();
	}
	
	
	/**
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the camFrame
	 */
	public ExtendedCamDetectionFrame getCamFrame()
	{
		return camFrame;
	}
	
}
