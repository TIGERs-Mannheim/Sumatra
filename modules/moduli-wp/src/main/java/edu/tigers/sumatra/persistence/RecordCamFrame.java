/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistence;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * Entity for vision data
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Entity
public class RecordCamFrame
{
	@PrimaryKey
	private final long								timestamp;
	
	private final ExtendedCamDetectionFrame	camFrame;
	
	
	@SuppressWarnings("unused")
	private RecordCamFrame()
	{
		timestamp = 0;
		camFrame = null;
	}
	
	
	/**
	 * @param camFrame to be saved
	 */
	public RecordCamFrame(final ExtendedCamDetectionFrame camFrame)
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
