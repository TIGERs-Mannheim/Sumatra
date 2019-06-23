/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import java.io.Serializable;
import java.util.Comparator;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.IVector2;


/**
 * Simple data holder describing objects that are recognized and tracked
 * 
 * @author Gero
 */
@Persistent(version = 2)
public abstract class ATrackedObject implements ITrackedObject
{
	private final long	timestamp;
	
	
	/**
	 */
	protected ATrackedObject()
	{
		timestamp = 0;
	}
	
	
	/**
	 * @param timestamp
	 */
	public ATrackedObject(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param o
	 */
	public ATrackedObject(final ITrackedObject o)
	{
		this(o.getTimestamp());
	}
	
	
	@Override
	public String toString()
	{
		return "[TrackedObject; pos = " + getPos() + " vel = " + getVel() + "]";
	}
	
	
	/**
	 * @return the pos
	 */
	@Override
	public abstract IVector2 getPos();
	
	
	/**
	 * @return the vel
	 */
	@Override
	public abstract IVector2 getVel();
	
	
	/**
	 * @return id
	 */
	@Override
	public abstract AObjectID getBotId();
	
	
	/**
	 * Compare ids of tracked objects
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class TrackedObjectComparator implements Comparator<ITrackedObject>, Serializable
	{
		/**  */
		private static final long	serialVersionUID	= -5304247749124149706L;
		
		
		@Override
		public int compare(final ITrackedObject o1, final ITrackedObject o2)
		{
			return o1.getBotId().compareTo(o2.getBotId());
		}
	}
	
	
	/**
	 * @return the timestamp
	 */
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
}
