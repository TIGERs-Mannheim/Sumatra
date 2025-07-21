/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.io.Serializable;
import java.util.Comparator;


/**
 * This is a point with a specific/arbitrary value.
 * This class is rather dangerous to use, because it extends from Vector2.
 * The behavior of equals is problematic. It can not be implemented such that the value is used and the contract of
 * equals is still given (bidirectional)...
 * Please consider creating a new class, if you need a similar data structure!
 * (Nicolai)
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class ValuePoint extends Vector2
{
	/**  */
	public static final Comparator<? super ValuePoint>	VALUE_HIGH_COMPARATOR	= new ValueHighComparator();
	/**  */
	public static final Comparator<? super ValuePoint>	VALUE_LOW_COMPARATOR		= new ValueLowComparator();
	
	private double													value							= 0;
	
	
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
		this.setValue(value);
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
		this.setValue(value);
	}
	
	
	/**
	 * @param copy
	 */
	public ValuePoint(final ValuePoint copy)
	{
		super(copy);
		setValue(copy.getValue());
	}
	
	
	/**
	 * @return the value
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
	
	
	@Override
	public String toString()
	{
		return "(x=" + x() + ",y=" + y() + ",val=" + getValue() + ")";
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
			if (v1.getValue() < v2.getValue())
			{
				return 1;
			} else if (v1.getValue() > v2.getValue())
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
			if (v1.getValue() > v2.getValue())
			{
				return 1;
			} else if (v1.getValue() < v2.getValue())
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
}