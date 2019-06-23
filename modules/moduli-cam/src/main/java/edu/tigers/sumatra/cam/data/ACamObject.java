/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Base class for SSL-Vision camera objects.
 */
@Persistent
public abstract class ACamObject implements IExportable
{
	/** often 1.0, unknown */
	private final double confidence;
	
	/** [pixel], left = 0, top = 0 */
	private final IVector2 pixel;
	
	private final long tCapture;
	private final long tAssembly;
	private final int camId;
	private final long frameId;
	
	
	protected ACamObject()
	{
		confidence = 0;
		pixel = Vector2f.ZERO_VECTOR;
		tCapture = 0;
		tAssembly = 0;
		camId = 0;
		frameId = 0;
	}
	
	
	/**
	 * @param confidence
	 * @param pixel
	 * @param tCapture
	 * @param camId
	 * @param frameId
	 */
	public ACamObject(final double confidence, final IVector2 pixel, final long tCapture,
			final int camId, final long frameId)
	{
		this.confidence = confidence;
		this.pixel = pixel.copy();
		this.tCapture = tCapture;
		this.tAssembly = System.nanoTime();
		this.camId = camId;
		this.frameId = frameId;
	}
	
	
	/**
	 * @param o
	 */
	public ACamObject(final ACamObject o)
	{
		confidence = o.confidence;
		pixel = o.pixel.copy();
		tCapture = o.tCapture;
		tAssembly = o.tAssembly;
		camId = o.camId;
		frameId = o.frameId;
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(getFrameId());
		numbers.add(getCameraId());
		numbers.add(gettCapture());
		numbers.add(gettAssembly());
		numbers.add(getPixel().x());
		numbers.add(getPixel().y());
		numbers.add(getConfidence());
		numbers.addAll(getPos().getNumberList());
		return numbers;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<>();
		headers.addAll(Arrays.asList("frameId", "camId", "tCapture", "tAssembly",
				"pixel_x", "pixel_y", "confidence"));
		if (getPos().getNumDimensions() >= 1)
		{
			headers.add("pos_x");
		}
		if (getPos().getNumDimensions() >= 2)
		{
			headers.add("pos_y");
		}
		if (getPos().getNumDimensions() >= 3)
		{
			headers.add("pos_z");
		}
		return headers;
	}
	
	
	/**
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
	public final long gettAssembly()
	{
		return tAssembly;
	}
}
