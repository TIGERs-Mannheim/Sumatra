/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Base class for SSL-Vision camera objects.
 */
@Getter
public abstract class ACamObject implements IExportable
{
	/**
	 * often 1.0, unknown
	 */
	private final double confidence;

	/**
	 * [pixel], left = 0, top = 0
	 */
	private final IVector2 pixel;

	private final long timestamp;
	private final int cameraId;
	private final long globalFrameId;


	protected ACamObject()
	{
		confidence = 0;
		pixel = Vector2f.ZERO_VECTOR;
		timestamp = 0;
		cameraId = 0;
		globalFrameId = 0;
	}


	protected ACamObject(
			final double confidence, final IVector2 pixel, final long timestamp, final int cameraId,
			final long globalFrameId
	)
	{
		this.confidence = confidence;
		this.pixel = pixel.copy();
		this.timestamp = timestamp;
		this.cameraId = cameraId;
		this.globalFrameId = globalFrameId;
	}


	protected ACamObject(final ACamObject o)
	{
		confidence = o.confidence;
		pixel = o.pixel.copy();
		timestamp = o.timestamp;
		cameraId = o.cameraId;
		globalFrameId = o.globalFrameId;
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(getGlobalFrameId());
		numbers.add(getCameraId());
		numbers.add(getTimestamp());
		numbers.add(getPixel().x());
		numbers.add(getPixel().y());
		numbers.add(getConfidence());
		numbers.addAll(getPos().getNumberList());
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<>(Arrays.asList(
				"frameId", "camId", "timestamp",
				"pixel_x", "pixel_y", "confidence"
		));
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
}
