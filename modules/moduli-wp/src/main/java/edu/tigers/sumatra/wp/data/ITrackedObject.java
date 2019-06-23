/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.data.ITimestampBased;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 */
public interface ITrackedObject extends IMirrorable<ITrackedObject>, ITimestampBased
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
	default IVector2 getAcc()
	{
		return Vector2f.zero();
	}
	
	
	/**
	 * @return id of the object
	 */
	AObjectID getId();
	
	
	/**
	 * @return timestamp in [ns]
	 */
	@Override
	long getTimestamp();
}