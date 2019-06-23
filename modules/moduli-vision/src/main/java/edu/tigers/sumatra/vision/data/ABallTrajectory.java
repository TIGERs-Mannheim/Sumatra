/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public abstract class ABallTrajectory
{
	protected final long	timestampNow;
	
	protected IVector3	kickPos;
	protected IVector3	kickVel;
	protected long			kickTimestamp;
	
	
	/**
	 * @param timestampNow
	 */
	public ABallTrajectory(final long timestampNow)
	{
		this.timestampNow = timestampNow;
	}
	
	
	/**
	 * Get state at specific timestamp.
	 * 
	 * @param timestamp
	 * @return
	 */
	public abstract FilteredVisionBall getStateAtTimestamp(final long timestamp);
	
	
	/**
	 * Get ball state with a relative time offset in [s] to "now".
	 * 
	 * @param timeOffset
	 * @return
	 */
	public FilteredVisionBall getState(final double timeOffset)
	{
		return getStateAtTimestamp(timestampNow + (long) (timeOffset * 1e9));
	}
	
	
	/**
	 * Get ball state right now.
	 * 
	 * @return
	 */
	public FilteredVisionBall getState()
	{
		return getStateAtTimestamp(timestampNow);
	}
	
	
	/**
	 * @return Kick position in [mm]
	 */
	public IVector2 getKickPos()
	{
		return kickPos.getXYVector();
	}
	
	
	/**
	 * @return Kick velocity in [mm/s]
	 */
	public IVector3 getKickVel()
	{
		return kickVel;
	}
	
	
	/**
	 * @return the kickTimestamp
	 */
	public long getKickTimestamp()
	{
		return kickTimestamp;
	}
	
	
	/**
	 * @return the timestampNow
	 */
	public long getTimestampNow()
	{
		return timestampNow;
	}
}
