/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector2} and
 * {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * @author Gero
 */
@Persistent
public abstract class AVector2 implements IVector2
{
	/** Vector2f(1,0) */
	public static final IVector2	X_AXIS		= new Vector2f(1, 0);
	/** Vector2f(0,1) */
	public static final IVector2	Y_AXIS		= new Vector2f(0, 1);
	/** Vector2f(0,0) */
	public static final IVector2	ZERO_VECTOR	= new Vector2f(0, 0);
	
	
	/**
	 * Only for code-reuse, not meant to be used by anyone anymore! =)
	 */
	AVector2()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float getLength2()
	{
		return SumatraMath.sqrt((x() * x()) + (y() * y()));
	}
	
	
	@Override
	public float getAngle()
	{
		if (isZeroVector())
		{
			assert false : "You try to calculate the angle between the X-Axis and a zero-vector!";
			return 0;
		}
		final float scalarProduct = scalarProduct(AVector2.X_AXIS);
		float result = AngleMath.acos(scalarProduct / (1 * getLength2()));
		result = AngleMath.normalizeAngle(result);
		if (y() < 0)
		{
			result = -result;
		}
		return result;
	}
	
	
	@Override
	public Vector2 addNew(final IVector2 vector)
	{
		final Vector2 result = new Vector2();
		if (vector != null)
		{
			result.x = x() + vector.x();
			result.y = y() + vector.y();
		} else
		{
			throw new NullPointerException("Input vector is null!");
		}
		
		return result;
	}
	
	
	@Override
	public Vector2 subtractNew(final IVector2 vector)
	{
		final Vector2 result = new Vector2();
		if (vector != null)
		{
			result.x = x() - vector.x();
			result.y = y() - vector.y();
		} else
		{
			throw new NullPointerException("Input vector is null!");
		}
		
		return result;
	}
	
	
	@Override
	public Vector2 multiplyNew(final float factor)
	{
		final Vector2 result = new Vector2();
		result.x = x() * factor;
		result.y = y() * factor;
		
		return result;
	}
	
	
	@Override
	public Vector2 scaleToNew(final float newLength)
	{
		final float oldLength = getLength2();
		if (oldLength != 0)
		{
			return multiplyNew(newLength / oldLength);
		}
		// You tried to scale a null-vector to a non-zero length! Vector stays unaffected.
		// but this is normal Math. if vector is zero, result is zero too
		return new Vector2(this);
	}
	
	
	@Override
	public Vector2 turnNew(final float angle)
	{
		float x2;
		float y2;
		
		float normAngle = AngleMath.normalizeAngle(angle);
		
		x2 = (x() * AngleMath.cos(normAngle)) - (y() * AngleMath.sin(normAngle));
		y2 = (y() * AngleMath.cos(normAngle)) + (x() * AngleMath.sin(normAngle));
		
		return new Vector2(x2, y2);
	}
	
	
	@Override
	public float scalarProduct(final IVector2 v)
	{
		return ((x() * v.x()) + (y() * v.y()));
	}
	
	
	@Override
	public Vector2 projectToNew(final IVector2 v)
	{
		if (v.isZeroVector())
		{
			return new Vector2(AVector2.ZERO_VECTOR);
		}
		return v.multiplyNew(scalarProduct(v) / SumatraMath.square(v.getLength2()));
	}
	
	
	@Override
	public Vector2 turnToNew(final float angle)
	{
		final float len = getLength2();
		final float yn = AngleMath.sin(angle) * len;
		final float xn = AngleMath.cos(angle) * len;
		return new Vector2(xn, yn);
	}
	
	
	@Override
	public IVector2 roundNew(final int digits)
	{
		final float newX;
		final float newY;
		float digitsLeft = digits;
		
		if (digitsLeft == 0)
		{
			newX = Math.round(x());
			newY = Math.round(y());
		} else if (digitsLeft > 0)
		{
			int f = 10;
			while (digitsLeft > 1)
			{
				f *= 10;
				--digitsLeft;
			}
			newX = (float) Math.round(x() * f) / f;
			newY = (float) Math.round(y() * f) / f;
		} else
		{
			newX = x();
			newY = y();
		}
		return new Vector2f(newX, newY);
	}
	
	
	@Override
	public Vector2 normalizeNew()
	{
		if (isZeroVector())
		{
			return new Vector2(this);
		}
		final float length = getLength2();
		return new Vector2(x() / length, y() / length);
	}
	
	
	@Override
	public IVector2 getNormalVector()
	{
		final Vector2 n = new Vector2(y(), -x());
		return n.normalize();
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof IVector2)
		{
			final IVector2 vec = (IVector2) obj;
			return (SumatraMath.isEqual(vec.x(), x()) && SumatraMath.isEqual(vec.y(), y()));
		}
		return false;
	}
	
	
	@Override
	public boolean equals(final IVector2 vec, final float tolerance)
	{
		if (vec == null)
		{
			return false;
		}
		return subtractNew(vec).getLength2() < tolerance;
	}
	
	
	@Override
	public int hashCode()
	{
		// Float.hashCode() == Float.floatToIntBits
		return ((17 * Float.floatToIntBits((x()))) ^ Float.floatToIntBits(y()));
	}
	
	
	@Override
	public boolean isZeroVector()
	{
		if ((x() == 0) && (y() == 0))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isVertical()
	{
		if ((x() == 0) && (y() != 0))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isHorizontal()
	{
		if ((y() == 0) && (x() != 0))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public Vector2 absNew()
	{
		return new Vector2(Math.abs(x()), Math.abs(y()));
	}
	
	
	@Override
	public String toString()
	{
		return "( " + x() + " / " + y() + " )";
	}
	
	
	@Override
	public String getSaveableString()
	{
		return x() + "," + y();
	}
	
	
	@Override
	public Vector2 turnAroundNew(final IVector2 axis, final float angle)
	{
		float x2 = x() - axis.x();
		float y2 = y() - axis.y();
		
		final float cosA = AngleMath.cos(angle);
		final float sinA = AngleMath.sin(angle);
		
		float x2old = x2;
		x2 = (x2 * cosA) - (y2 * sinA);
		y2 = (y2 * cosA) + (x2old * sinA);
		
		x2 = x2 + axis.x();
		y2 = y2 + axis.y();
		
		return new Vector2(x2, y2);
	}
}
