/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.11.2010
 * Author(s): Yakisoba
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import com.sleepycat.persist.model.Persistent;


/**
 *
 */
@Persistent
public abstract class ACamObject
{
	/** often 1.0, unknown */
	private final float	confidence;
	
	/** px, left = 0 */
	private final float	pixelX;
	
	/** px, top = 0 */
	private final float	pixelY;
	
	private final long	timestamp;
	private final int		camId;
	
	
	protected ACamObject()
	{
		confidence = 0;
		pixelX = 0;
		pixelY = 0;
		timestamp = 0;
		camId = 0;
	}
	
	
	/**
	 * Enum to identify implementation of inherited class
	 * 
	 * @author KaiE
	 */
	public static enum ECamObjectType
	{
		/** See {@link CamBall} */
		Ball,
		/** See {@link CamRobot} */
		Robot,
		/** not specified */
		Unknown
	}
	
	
	/**
	 * @param confidence
	 * @param pixelX
	 * @param pixelY
	 * @param timestamp
	 * @param camId
	 */
	public ACamObject(final float confidence, final float pixelX, final float pixelY, final long timestamp,
			final int camId)
	{
		this.confidence = confidence;
		this.pixelX = pixelX;
		this.pixelY = pixelY;
		this.timestamp = timestamp;
		this.camId = camId;
	}
	
	
	/**
	 * This method has been added to allow a cheap comparison to cast from Base-Class to
	 * implementation
	 * 
	 * @author KaiE
	 * @return
	 */
	public ECamObjectType implementation()
	{
		return ECamObjectType.Unknown;
	}
	
	
	/**
	 * @return the pixelY
	 */
	public float getPixelY()
	{
		return pixelY;
	}
	
	
	/**
	 * @return the pixelX
	 */
	public float getPixelX()
	{
		return pixelX;
	}
	
	
	/**
	 * @return the confidence
	 */
	public float getConfidence()
	{
		return confidence;
	}
	
	
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the camId
	 */
	public final int getCameraId()
	{
		return camId;
	}
}
