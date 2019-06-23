/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrackedObject extends IMirrorable<ITrackedObject>
{
	
	
	/**
	 * @return the pos
	 */
	IVector2 getPos();
	
	
	/**
	 * @return the vel
	 */
	IVector2 getVel();
	
	
	/**
	 * @return the acc
	 */
	IVector2 getAcc();
	
	
	/**
	 * @return id of the object
	 */
	AObjectID getId();
	
	
	/**
	 * @return timestamp in [ns]
	 */
	long getTimestamp();
	
	
}