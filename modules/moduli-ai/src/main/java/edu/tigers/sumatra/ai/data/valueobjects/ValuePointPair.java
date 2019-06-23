/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.04.2011
 * Author(s): GuntherB
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.valueobjects;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2f;


/**
 * Just like {@link ValuePoint}, just that this is a set of Vector2f's with a value.
 * 
 * @author GuntherB
 */
public class ValuePointPair implements Comparable<ValuePointPair>
{
	/** */
	public final Vector2f	point1;
	/** */
	public final Vector2f	point2;
	
	private double				value;
	
	
	/**
	 * @param point1
	 * @param point2
	 */
	public ValuePointPair(final Vector2f point1, final Vector2f point2)
	{
		this.point1 = point1;
		this.point2 = point2;
	}
	
	
	/**
	 * @param point1
	 * @param point2
	 */
	public ValuePointPair(final ValuePoint point1, final ValuePoint point2)
	{
		this.point1 = new Vector2f(point1);
		this.point2 = new Vector2f(point2);
	}
	
	
	@Override
	public int compareTo(final ValuePointPair otherPair)
	{
		if (otherPair.getValue() > getValue())
		{
			return 1;
		} else if (otherPair.getValue() < getValue())
		{
			return -1;
		} else
		{
			return 0;
		}
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((point1 == null) ? 0 : point1.hashCode());
		result = (prime * result) + ((point2 == null) ? 0 : point2.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if ((obj == null) || (getClass() != obj.getClass()))
		{
			return false;
		}
		
		final ValuePointPair other = (ValuePointPair) obj;
		if (point1 == null)
		{
			if (other.point1 != null)
			{
				return false;
			}
		} else if (!point1.equals(other.point1))
		{
			return false;
		}
		
		if (point2 == null)
		{
			if (other.point2 != null)
			{
				return false;
			}
		} else if (!point2.equals(other.point2))
		{
			return false;
		}
		if (SumatraMath.isEqual(value, other.value))
		{
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * @return
	 */
	public double getValue()
	{
		return value;
	}
	
	
	/**
	 * @param value
	 */
	public void setValue(final double value)
	{
		this.value = value;
	}
}
