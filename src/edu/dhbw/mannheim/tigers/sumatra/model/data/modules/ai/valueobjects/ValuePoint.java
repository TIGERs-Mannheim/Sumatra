/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects;

import java.io.Serializable;
import java.util.Comparator;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * This is a point with a specific/arbitrary value.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
@Embeddable
public class ValuePoint extends Vector2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long									serialVersionUID		= -6549729498617629967L;
	
	/** */
	public static final Comparator<? super ValuePoint>	YCOMPARATOR				= new YLowComparator();
	/**  */
	public static final Comparator<? super ValuePoint>	VALUEHIGHCOMPARATOR	= new ValueHighComparator();
	/**  */
	public static final Comparator<? super ValuePoint>	VALUELOWCOMPARATOR	= new ValueLowComparator();
	/**  */
	public float													value						= 0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public ValuePoint(float x, float y)
	{
		super(x, y);
	}
	
	
	/**
	 * 
	 * @param vec
	 * @param value
	 */
	public ValuePoint(IVector2 vec, float value)
	{
		super(vec);
		this.value = value;
	}
	
	
	/**
	 * 
	 * @param vec
	 */
	public ValuePoint(IVector2 vec)
	{
		super(vec);
	}
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param value
	 */
	public ValuePoint(float x, float y, float value)
	{
		super(x, y);
		this.value = value;
	}
	
	
	/**
	 * 
	 * @param copy
	 */
	public ValuePoint(ValuePoint copy)
	{
		super(copy);
		value = copy.value;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @return
	 */
	public float getValue()
	{
		return value;
	}
	
	
	/**
	 * 
	 * @param value
	 */
	public void setValue(float value)
	{
		this.value = value;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		return Float.floatToIntBits(value);
	}
	
	
	@Override
	public String toString()
	{
		return "(x=" + x + ",y=" + y + ",val=" + value + ")";
	}
	
	
	@Override
	public boolean equals(Object obj)
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
	 * 
	 * Sort {@link ValuePoint} after Value, lowest value first.
	 * 
	 */
	private static class YLowComparator implements Comparator<ValuePoint>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(ValuePoint v1, ValuePoint v2)
		{
			if (v1.y() > v2.y())
			{
				return 1;
			} else if (v1.y() < v2.y())
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * Sort {@link ValuePoint} after Value, highest value first.
	 * 
	 */
	private static class ValueHighComparator implements Comparator<ValuePoint>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(ValuePoint v1, ValuePoint v2)
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
	 * 
	 * Sort {@link ValuePoint} after Value, lowest value first.
	 * 
	 */
	private static class ValueLowComparator implements Comparator<ValuePoint>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(ValuePoint v1, ValuePoint v2)
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