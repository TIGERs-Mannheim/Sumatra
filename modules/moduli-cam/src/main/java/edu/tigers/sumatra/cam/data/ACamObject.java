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
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Base class for SSL-Vision camera objects.
 */
@Persistent
public abstract class ACamObject implements INumberListable
{
	/** often 1.0, unknown */
	private final double		confidence;
	
	/** [pixel], left = 0, top = 0 */
	private final IVector2	pixel;
	
	private final long		tCapture;
	private final long		tSent;
	private final int			camId;
	private final long		frameId;
	
	
	protected ACamObject()
	{
		confidence = 0;
		pixel = AVector2.ZERO_VECTOR;
		tCapture = 0;
		tSent = 0;
		camId = 0;
		frameId = 0;
	}
	
	
	/**
	 * @param confidence
	 * @param pixel
	 * @param tCapture
	 * @param tSent
	 * @param camId
	 * @param frameId
	 */
	public ACamObject(final double confidence, final IVector2 pixel, final long tCapture,
			final long tSent, final int camId, final long frameId)
	{
		this.confidence = confidence;
		this.pixel = pixel.copy();
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
		pixel = o.pixel.copy();
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
	 * @return the pixel
	 */
	public IVector2 getPixel()
	{
		return pixel;
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
