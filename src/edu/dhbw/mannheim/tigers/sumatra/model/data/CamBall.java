/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionBall;


/**
 * Simple data holder; internal data structure for the {@link SSL_DetectionBall} from protobuf-protocol, coming from the
 * SSL-Vision.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * @author Gero
 * 
 */
public class CamBall extends ACamObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** ? */
	public final int			area;
	
	/** mm, (NA in current SSLVision) */
	public final Vector3f	pos;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
	 */
	public CamBall(float confidence, int area, float x, float y, float z, float pixelX, float pixelY)
	{
		super(confidence, pixelX, pixelY);
		this.area = area;
		this.pos = new Vector3f(x, y, z);
	}
	

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SSLBall [area=");
		builder.append(area);
		builder.append(", confidence=");
		builder.append(confidence);
		builder.append(", pixelX=");
		builder.append(pixelX);
		builder.append(", pixelY=");
		builder.append(pixelY);
		builder.append(", x=");
		builder.append(pos.x);
		builder.append(", y=");
		builder.append(pos.y);
		builder.append(", z=");
		builder.append(pos.z);
		builder.append("]");
		return builder.toString();
	}
}
