/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;


/**
 * Simple data holder; internal data structure for the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionBall} from
 * protobuf-protocol, coming from the
 * SSL-Vision.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 */
@Persistent
public class CamBall extends ACamObject
{
	private final int			area;
	
	/** mm, (NA in current SSLVision) */
	private final IVector3	pos;
	
	
	@SuppressWarnings("unused")
	private CamBall()
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
	 * @param x
	 * @param y
	 * @param z
	 * @param pixelX
	 * @param pixelY
	 * @param timestamp
	 * @param camId
	 */
	public CamBall(final float confidence, final int area, final float x, final float y, final float z,
			final float pixelX, final float pixelY, final long timestamp, final int camId)
	{
		super(confidence, pixelX, pixelY, timestamp, camId);
		this.area = area;
		pos = new Vector3f(x, y, z);
	}
	
	
	/**
	 * @param newCamBall
	 */
	public CamBall(final CamBall newCamBall)
	{
		super(newCamBall.getConfidence(), newCamBall.getPixelX(), newCamBall.getPixelY(), newCamBall.getTimestamp(),
				newCamBall.getCameraId());
		area = newCamBall.getArea();
		pos = new Vector3f(newCamBall.getPos());
	}
	
	
	/**
	 * @return
	 */
	public static CamBall defaultInstance()
	{
		return new CamBall(0, 0, 0, 0, 0, 0, 0, 0, 0);
	}
	
	
	@Override
	public ECamObjectType implementation()
	{
		return ECamObjectType.Ball;
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
		builder.append(getPixelX());
		builder.append(", pixelY=");
		builder.append(getPixelY());
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
	public IVector3 getPos()
	{
		return pos;
	}
}
