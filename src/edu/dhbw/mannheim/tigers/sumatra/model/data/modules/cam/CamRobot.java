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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


/**
 * Simple data holder; internal data structure for the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionRobot} from
 * protobuf-protocol, coming from
 * the SSL-Vision.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 */
@Persistent
public class CamRobot extends ACamObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private final int			robotID;
	
	/** mm */
	private final IVector2	pos;
	
	/**  */
	private final float		orientation;
	
	/**  */
	private final float		height;
	
	
	@SuppressWarnings("unused")
	private CamRobot()
	{
		super();
		robotID = 0;
		pos = AVector2.ZERO_VECTOR;
		orientation = 0;
		height = 0;
	}
	
	
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
	 * @param timestamp
	 * @param camId
	 */
	public CamRobot(final float confidence, final int robotID, final float x, final float y, final float orientation,
			final float pixelX, final float pixelY,
			final float height, final long timestamp, final int camId)
	{
		super(confidence, pixelX, pixelY, timestamp, camId);
		this.robotID = robotID;
		pos = new Vector2f(x, y);
		this.orientation = orientation;
		this.height = height;
	}
	
	
	@Override
	public ECamObjectType implementation()
	{
		return ECamObjectType.Robot;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SSLRobot [confidence=");
		builder.append(getConfidence());
		builder.append(", height=");
		builder.append(getHeight());
		builder.append(", orientation=");
		builder.append(getOrientation());
		builder.append(", pixelX=");
		builder.append(getPixelX());
		builder.append(", pixelY=");
		builder.append(getPixelY());
		builder.append(", robotID=");
		builder.append(getRobotID());
		builder.append(", x=");
		builder.append(getPos().x());
		builder.append(", y=");
		builder.append(getPos().y());
		builder.append("]");
		return builder.toString();
	}
	
	
	/**
	 * @return the robotID
	 */
	public int getRobotID()
	{
		return robotID;
	}
	
	
	/**
	 * @return the pos
	 */
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return the orientation
	 */
	public float getOrientation()
	{
		return orientation;
	}
	
	
	/**
	 * @return the height
	 */
	public float getHeight()
	{
		return height;
	}
}
