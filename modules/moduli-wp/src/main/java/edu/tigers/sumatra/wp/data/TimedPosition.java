/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * Position which carries a timestamp corresponding to the time it was captured
 */
public class TimedPosition
{
	private final long timestamp;
	private final IVector3 position;
	
	
	/**
	 * default constructor
	 */
	public TimedPosition()
	{
		timestamp = 0;
		position = Vector3f.ZERO_VECTOR;
	}
	
	
	/**
	 * @param timestamp
	 * @param position
	 */
	public TimedPosition(final long timestamp, final IVector3 position)
	{
		this.position = position;
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @param currentTimestamp
	 * @return the age in [s]
	 */
	public double getAge(final long currentTimestamp)
	{
		return (currentTimestamp - timestamp) / 1e9;
	}
	
	
	/**
	 * @return the ts
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	public IVector2 getPos()
	{
		return position.getXYVector();
	}
	
	
	public IVector3 getPos3()
	{
		return position;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final TimedPosition that = (TimedPosition) o;
		
		return new EqualsBuilder()
				.append(timestamp, that.timestamp)
				.append(position, that.position)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(timestamp)
				.append(position)
				.toHashCode();
	}
	
	
	/**
	 * @param other
	 * @return true, if other is similar to this
	 */
	public boolean similarTo(final TimedPosition other)
	{
		if (other == null)
		{
			return false;
		}
		boolean similarInTime = Math.abs(timestamp - other.timestamp) < 2e9;
		boolean similarInSpace = position.subtractNew(other.position).getLength() < 200 * 200;
		return similarInTime && similarInSpace;
	}
}
