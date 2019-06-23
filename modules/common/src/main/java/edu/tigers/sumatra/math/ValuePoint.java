/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.io.Serializable;
import java.util.Comparator;

import com.sleepycat.persist.model.Persistent;


/**
 * This is a point with a specific/arbitrary value.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
@Persistent
public class ValuePoint extends Vector2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	public static final Comparator<? super ValuePoint>	VALUE_HIGH_COMPARATOR	= new ValueHighComparator();
	/**  */
	public static final Comparator<? super ValuePoint>	VALUE_LOW_COMPARATOR		= new ValueLowComparator();
	/**  */
	public double													value							= 0;
																										
																										
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	protected ValuePoint()
	{
	}
	
	
	/**
	 * @param x
	 * @param y
	 */
	public ValuePoint(final double x, final double y)
	{
		super(x, y);
	}
	
	
	/**
	 * @param vec
	 * @param value
	 */
	public ValuePoint(final IVector2 vec, final double value)
	{
		super(vec);
		this.value = value;
	}
	
	
	/**
	 * @param vec
	 */
	public ValuePoint(final IVector2 vec)
	{
		super(vec);
	}
	
	
	/**
	 * @param x
	 * @param y
	 * @param value
	 */
	public ValuePoint(final double x, final double y, final double value)
	{
		super(x, y);
		this.value = value;
	}
	
	
	/**
	 * @param copy
	 */
	public ValuePoint(final ValuePoint copy)
	{
		super(copy);
		value = copy.value;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public synchronized int hashCode()
	{
		long temp;
		temp = Double.doubleToLongBits(value);
		return (int) (temp ^ (temp >>> 32));
	}
	
	
	@Override
	public synchronized String toString()
	{
		return "(x=" + x() + ",y=" + y() + ",val=" + value + ")";
	}
	
	
	@Override
	public synchronized boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (!super.equals(obj) || (getClass() != obj.getClass()))
		{
			return false;
		}
		
		final ValuePoint other = (ValuePoint) obj;
		if (!SumatraMath.isEqual(value, other.value))
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Sort {@link ValuePoint} after Value, highest value first.
	 */
	private static class ValueHighComparator implements Comparator<ValuePoint>, Serializable
	{
		
		/**  */
		private static final long serialVersionUID = 1794858044291002364L;
		
		
		@Override
		public int compare(final ValuePoint v1, final ValuePoint v2)
		{
			if (v1.value < v2.value)
			{
				return 1;
			} else if (v1.value > v2.value)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	/**
	 * Sort {@link ValuePoint} after Value, lowest value first.
	 */
	private static class ValueLowComparator implements Comparator<ValuePoint>, Serializable
	{
		
		/**  */
		private static final long serialVersionUID = 1794858044291002364L;
		
		
		@Override
		public int compare(final ValuePoint v1, final ValuePoint v2)
		{
			if (v1.value > v2.value)
			{
				return 1;
			} else if (v1.value < v2.value)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
}