/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Simple data holder; internal data structure for the protobuf-protocol, coming from the SSL-Vision.
 * 
 * @author Gero
 */
@Persistent
public class CamBall extends ACamObject
{
	private final int			area;
	
	/** [mm], (z == 0 in current SSLVision) */
	private final IVector3	pos;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public CamBall()
	{
		super();
		area = 0;
		pos = AVector3.ZERO_VECTOR;
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
	 * @param tSent
	 * @param camId
	 * @param frameId
	 */
	@SuppressWarnings("squid:S00107")
	public CamBall(final double confidence, final int area, final IVector3 pos,
			final IVector2 pixel, final long tCapture, final long tSent, final int camId,
			final long frameId)
	{
		super(confidence, pixel, tCapture, tSent, camId, frameId);
		this.area = area;
		this.pos = pos;
	}
	
	
	/**
	 * @param base
	 * @param pos
	 */
	public CamBall(final CamBall base, final IVector2 pos)
	{
		this(base.getConfidence(), base.getArea(), Vector3.fromXYZ(pos.x(), pos.y(), base.getPos().z()), base.getPixel(),
				base.gettCapture(), base.gettSent(), base.getCameraId(), base.getFrameId());
	}
	
	
	/**
	 * @param newCamBall
	 */
	public CamBall(final CamBall newCamBall)
	{
		super(newCamBall.getConfidence(), newCamBall.getPixel(), newCamBall.gettCapture(),
				newCamBall.gettSent(), newCamBall.getCameraId(), newCamBall.getFrameId());
		area = newCamBall.getArea();
		pos = Vector3.copy(newCamBall.getPos());
	}
	
	
	/**
	 * @param list
	 * @return
	 */
	public static CamBall fromNumberList(final List<? extends Number> list)
	{
		final long tCapture = list.get(0).longValue();
		final int camId = list.get(1).intValue();
		final double x = list.get(2).doubleValue();
		final double y = list.get(3).doubleValue();
		final double z = list.get(4).doubleValue();
		final long frameId = list.size() <= 5 ? 0 : list.get(5).longValue();
		final double pixelX = list.size() <= 6 ? 0 : list.get(6).doubleValue();
		final double pixelY = list.size() <= 7 ? 0 : list.get(7).doubleValue();
		final int area = list.size() <= 8 ? 0 : list.get(8).intValue();
		final double confidence = list.size() <= 9 ? 0 : list.get(9).doubleValue();
		final long tSent = list.size() <= 10 ? 0 : list.get(10).longValue();
		
		return new CamBall(confidence, area, Vector3.fromXYZ(x, y, z), Vector2.fromXY(pixelX, pixelY), tCapture, tSent,
				camId, frameId);
	}
	
	
	@Override
	public String toString()
	{
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
		List<Number> numbers = new ArrayList<>();
		numbers.add(gettCapture());
		numbers.add(getCameraId());
		numbers.addAll(pos.getNumberList());
		numbers.add(getFrameId());
		numbers.add(getPixel().x());
		numbers.add(getPixel().y());
		numbers.add(getArea());
		numbers.add(getConfidence());
		numbers.add(gettSent());
		return numbers;
	}
}
