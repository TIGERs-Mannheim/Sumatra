/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple data holder; internal data structure for the protobuf-protocol, coming from the SSL-Vision.
 */
@Getter
public class CamBall extends ACamObject
{
	private final int area;

	/**
	 * [mm], (z == 0 in current SSLVision)
	 */
	private final IVector3 pos;


	public CamBall()
	{
		super();
		area = 0;
		pos = Vector3f.ZERO_VECTOR;
	}


	public CamBall(
			double confidence, int area, IVector3 pos,
			IVector2 pixel, long tCapture, int camId, long globalFrameId
	)
	{
		super(confidence, pixel, tCapture, camId, globalFrameId);
		this.area = area;
		this.pos = pos;
	}


	public CamBall(final CamBall newCamBall)
	{
		super(newCamBall);
		area = newCamBall.getArea();
		pos = Vector3.copy(newCamBall.getPos());
	}


	@Override
	public String toString()
	{
		// noinspection StringBufferReplaceableByString
		StringBuilder builder = new StringBuilder();
		builder.append("SSLBall [timestamp=");
		builder.append(getTimestamp());
		builder.append(", camId=");
		builder.append(getCameraId());
		builder.append(", x=");
		builder.append(getPos().x());
		builder.append(", y=");
		builder.append(getPos().y());
		builder.append(", z=");
		builder.append(getPos().z());
		builder.append(", area=");
		builder.append(getArea());
		builder.append(", confidence=");
		builder.append(getConfidence());
		builder.append(", pixelX=");
		builder.append(getPixel().x());
		builder.append(", pixelY=");
		builder.append(getPixel().y());
		builder.append("]");
		return builder.toString();
	}


	@Override
	public IVector3 getPos()
	{
		return pos;
	}


	public IVector2 getFlatPos()
	{
		return pos.getXYVector();
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = super.getNumberList();
		numbers.add(area);
		numbers.add(pos.z());
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<>(super.getHeaders());
		headers.add("area");
		headers.add("height");
		return headers;
	}
}
