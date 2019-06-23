/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


/**
 * Simple data holder; internal data structure for the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionRobot} from
 * protobuf-protocol, coming from
 * the SSL-Vision.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * @author Gero
 * 
 */
public class CamRobot extends ACamObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	public final int			robotID;
	
	/** mm */
	public final Vector2f	pos;
	
	/**  */
	public final float		orientation;
	
	/**  */
	public final float		height;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * <p>
	 * <i>(Being aware of EJ-SE Item 2; but we prefer performance over readability - at least in this case. Objects are
	 * created at only one point in the system, but needs to be fast.</i>
	 * </p>
	 * 
	 * @param confidence
	 * @param robotID
	 * @param x
	 * @param y
	 * @param orientation
	 * @param pixelX
	 * @param pixelY
	 * @param height
	 */
	public CamRobot(float confidence, int robotID, float x, float y, float orientation, float pixelX, float pixelY,
			float height)
	{
		super(confidence, pixelX, pixelY);
		this.robotID = robotID;
		pos = new Vector2f(x, y);
		this.orientation = orientation;
		this.height = height;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SSLRobot [confidence=");
		builder.append(confidence);
		builder.append(", height=");
		builder.append(height);
		builder.append(", orientation=");
		builder.append(orientation);
		builder.append(", pixelX=");
		builder.append(pixelX);
		builder.append(", pixelY=");
		builder.append(pixelY);
		builder.append(", robotID=");
		builder.append(robotID);
		builder.append(", x=");
		builder.append(pos.x());
		builder.append(", y=");
		builder.append(pos.y());
		builder.append("]");
		return builder.toString();
	}
}
