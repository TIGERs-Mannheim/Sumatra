/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.sleepycat.persist.model.Persistent;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector2} and
 * {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * @author Gero
 */
@Persistent(version = 1)
public abstract class AVector2 extends AVector implements IVector2
{
	/** Vector2f(1,0) */
	public static final IVector2								X_AXIS			= new Vector2f(1, 0);
	/** Vector2f(0,1) */
	public static final IVector2								Y_AXIS			= new Vector2f(0, 1);
	/** Vector2f(0,0) */
	public static final IVector2								ZERO_VECTOR		= new Vector2f(0, 0);
	/**  */
	public static final Comparator<? super IVector2>	Y_COMPARATOR	= new YComparator();
	
	
	/**
	 * The String must be a pair of comma or space separated double values.
	 * Additional spaces are considered
	 * 
	 * @param value
	 * @return
	 * @throws NumberFormatException
	 */
	public static IVector2 valueOf(final String value)
	{
		String[] values = value.replaceAll("[,;]", " ").split("[ ]");
		List<String> finalValues = new ArrayList<String>(2);
		for (String val : values)
		{
			if (!val.trim().isEmpty() && !val.contains(","))
			{
				finalValues.add(val.trim());
			}
		}
		if (finalValues.size() != 2)
		{
			throw new NumberFormatException(
					"The String must contain exactly one character of [ ,;] between x and y coordinate. Values: "
							+ finalValues);
		}
		return new Vector2f(Double.valueOf(finalValues.get(0)), Double.valueOf(finalValues.get(1)));
	}
	
	
	/**
	 * @param i
	 * @return
	 */
	@Override
	public double get(final int i)
	{
		switch (i)
		{
			case 0:
				return x();
			case 1:
				return y();
		}
		throw new IllegalArgumentException("Invalid index: " + i);
	}
	
	
	@Override
	public synchronized double getAngle()
	{
		if (isZeroVector())
		{
			assert false : "You try to calculate the angle between the X-Axis and a zero-vector!";
			return 0;
		}
		final double scalarProduct = scalarProduct(AVector2.X_AXIS);
		double result = AngleMath.acos(scalarProduct / (1.0 * getLength2()));
		result = AngleMath.normalizeAngle(result);
		if (y() < 0)
		{
			result = -result;
		}
		return result;
	}
	
	
	@Override
	public synchronized double getAngle(final double defAngle)
	{
		if (isZeroVector())
		{
			return defAngle;
		}
		return getAngle();
	}
	
	
	@Override
	public synchronized Vector2 addNew(final IVector vector)
	{
		final Vector2 result = new Vector2();
		if (vector != null)
		{
			result.setX(x() + vector.x());
			result.setY(y() + vector.y());
		} else
		{
			throw new NullPointerException("Input vector is null!");
		}
		
		return result;
	}
	
	
	@Override
	public synchronized Vector2 subtractNew(final IVector vector)
	{
		final Vector2 result = new Vector2();
		if (vector != null)
		{
			result.setX(x() - vector.x());
			result.setY(y() - vector.y());
		} else
		{
			throw new NullPointerException("Input vector is null!");
		}
		
		return result;
	}
	
	
	@Override
	public synchronized Vector2 multiplyNew(final double factor)
	{
		final Vector2 result = new Vector2();
		result.setX(x() * factor);
		result.setY(y() * factor);
		
		return result;
	}
	
	
	@Override
	public synchronized Vector2 multiplyNew(final IVector vector)
	{
		final Vector2 result = new Vector2(
				x() * vector.x(),
				y() * vector.y());
		
		return result;
	}
	
	
	@Override
	public synchronized Vector2 scaleToNew(final double newLength)
	{
		final double oldLength = getLength2();
		if (oldLength != 0)
		{
			return multiplyNew(newLength / oldLength);
		}
		// You tried to scale a null-vector to a non-zero length! Vector stays unaffected.
		// but this is normal Math. if vector is zero, result is zero too
		return new Vector2(this);
	}
	
	
	@Override
	public synchronized Vector2 turnNew(final double angle)
	{
		double x2;
		double y2;
		
		double normAngle = AngleMath.normalizeAngle(angle);
		
		x2 = (x() * AngleMath.cos(normAngle)) - (y() * AngleMath.sin(normAngle));
		y2 = (y() * AngleMath.cos(normAngle)) + (x() * AngleMath.sin(normAngle));
		
		return new Vector2(x2, y2);
	}
	
	
	@Override
	public synchronized Vector2 turnAroundNew(final IVector2 axis, final double angle)
	{
		double x2 = x() - axis.x();
		double y2 = y() - axis.y();
		
		final double cosA = AngleMath.cos(angle);
		final double sinA = AngleMath.sin(angle);
		
		double x2old = x2;
		x2 = (x2 * cosA) - (y2 * sinA);
		y2 = (y2 * cosA) + (x2old * sinA);
		
		x2 = x2 + axis.x();
		y2 = y2 + axis.y();
		
		return new Vector2(x2, y2);
	}
	
	
	@Override
	public synchronized Vector2 applyNew(final Function<Double, Double> function)
	{
		Vector2 result = new Vector2(
				function.apply(x()),
				function.apply(y()));
		return result;
	}
	
	
	@Override
	public synchronized double scalarProduct(final IVector2 v)
	{
		return ((x() * v.x()) + (y() * v.y()));
	}
	
	
	@Override
	public synchronized Vector2 turnToNew(final double angle)
	{
		final double len = getLength2();
		final double yn = AngleMath.sin(angle) * len;
		final double xn = AngleMath.cos(angle) * len;
		return new Vector2(xn, yn);
	}
	
	
	@Override
	public synchronized Vector2 normalizeNew()
	{
		if (isZeroVector())
		{
			return new Vector2(this);
		}
		final double length = getLength2();
		return new Vector2(x() / length, y() / length);
	}
	
	
	@Override
	public synchronized Vector2 absNew()
	{
		return applyNew(f -> Math.abs(f));
	}
	
	
	@Override
	public synchronized Vector2 getNormalVector()
	{
		return new Vector2(y(), -x());
	}
	
	
	@Override
	public synchronized boolean isVertical()
	{
		if ((x() == 0) && (y() != 0))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public synchronized boolean isHorizontal()
	{
		if ((y() == 0) && (x() != 0))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public Vector3 getXYZVector()
	{
		return new Vector3(this, 0);
	}
	
	private static class YComparator implements Comparator<IVector2>, Serializable
	{
		private static final long serialVersionUID = 1794858044291002364L;
		
		
		@Override
		public int compare(final IVector2 v1, final IVector2 v2)
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
}
