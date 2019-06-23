/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.11.2010
 * Author(s): Yakisoba
 * *********************************************************
 */
package edu.tigers.sumatra.cam.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.export.INumberListable;
import edu.tigers.sumatra.math.IVector;


/**
 *
 */
@Persistent
public abstract class ACamObject implements INumberListable
{
	/** often 1.0, unknown */
	private final double	confidence;
	
	/** px, left = 0 */
	private final double	pixelX;
	
	/** px, top = 0 */
	private final double	pixelY;
	
	private final long	tCapture;
	private final long	tSent;
	private final int		camId;
	private final long	frameId;
	
	
	protected ACamObject()
	{
		confidence = 0;
		pixelX = 0;
		pixelY = 0;
		tCapture = 0;
		tSent = 0;
		camId = 0;
		frameId = 0;
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
	 * @param tCapture
	 * @param tSent
	 * @param camId
	 * @param frameId
	 */
	public ACamObject(final double confidence, final double pixelX, final double pixelY, final long tCapture,
			final long tSent,
			final int camId, final long frameId)
	{
		this.confidence = confidence;
		this.pixelX = pixelX;
		this.pixelY = pixelY;
		this.tCapture = tCapture;
		this.tSent = tSent;
		this.camId = camId;
		this.frameId = frameId;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param o
	 */
	public ACamObject(final ACamObject o)
	{
		confidence = o.confidence;
		pixelX = o.pixelX;
		pixelY = o.pixelY;
		tCapture = o.tCapture;
		tSent = o.tSent;
		camId = o.camId;
		frameId = o.frameId;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	public abstract IVector getPos();
	
	
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
	public double getPixelY()
	{
		return pixelY;
	}
	
	
	/**
	 * @return the pixelX
	 */
	public double getPixelX()
	{
		return pixelX;
	}
	
	
	/**
	 * @return the confidence
	 */
	public double getConfidence()
	{
		return confidence;
	}
	
	
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp()
	{
		return tCapture;
	}
	
	
	/**
	 * @return the camId
	 */
	public final int getCameraId()
	{
		return camId;
	}
	
	
	/**
	 * @return the frameId
	 */
	public final long getFrameId()
	{
		return frameId;
	}
	
	
	/**
	 * @return the tCapture
	 */
	public final long gettCapture()
	{
		return tCapture;
	}
	
	
	/**
	 * @return the tSent
	 */
	public final long gettSent()
	{
		return tSent;
	}
}
