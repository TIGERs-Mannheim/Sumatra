/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * Simple data holder; internal data structure for the protobuf-protocol, coming from the SSL-Vision.
 * 
 * @author Gero
 */
@Persistent
public class CamBall extends ACamObject
{
	private final int area;
	
	/** [mm], (z == 0 in current SSLVision) */
	private final IVector3 pos;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public CamBall()
	{
		super();
		area = 0;
		pos = Vector3f.ZERO_VECTOR;
	}
	
	
	/**
	 * <p>
	 * <i>Implemented being aware of EJSE Item 2; but we prefer performance over readability - at least in this case.
	 * Objects are created at only one point in the system, but needs to be fast (so builder seems to be too much
	 * overhead).</i>
	 * </p>
	 * 
	 * @param confidence
	 * @param area
	 * @param pos
	 * @param pixel
	 * @param tCapture
	 * @param camId
	 * @param frameId
	 */
	public CamBall(final double confidence, final int area, final IVector3 pos,
			final IVector2 pixel, final long tCapture, final int camId,
			final long frameId)
	{
		super(confidence, pixel, tCapture, camId, frameId);
		this.area = area;
		this.pos = pos;
	}
	
	
	/**
	 * @param newCamBall
	 */
	public CamBall(final CamBall newCamBall)
	{
		super(newCamBall);
		area = newCamBall.getArea();
		pos = Vector3.copy(newCamBall.getPos());
	}
	
	
	/**
	 * New CamBall with adjusted tCapture.
	 * 
	 * @param newCamBall
	 * @param tCapture
	 */
	public CamBall(final CamBall newCamBall, final long tCapture)
	{
		super(newCamBall.getConfidence(), newCamBall.getPixel(), tCapture, newCamBall.getCameraId(),
				newCamBall.getFrameId());
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
	
	
	/**
	 * @return the area
	 */
	public int getArea()
	{
		return area;
	}
	
	
	/**
	 * @return the pos
	 */
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
