/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector2} and
 * {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * 
 * @author Gero
 * 
 */
public abstract class AVector2 implements IVector2
{
	/** Vector2f(1,0) */
	public static final Vector2f	X_AXIS		= new Vector2f(1, 0);
	/** Vector2f(0,1) */
	public static final Vector2f	Y_AXIS		= new Vector2f(0, 1);
	/** Vector2f(0,0) */
	public static final Vector2f	ZERO_VECTOR	= new Vector2f(0, 0);
	
	protected static final Logger	LOG			= Logger.getLogger(AVector2.class);
	
	
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
		return AIMath.sqrt(x() * x() + y() * y());
	}
	

	@Override
	public float getAngle()
	{
		if (this.isZeroVector())
		{
			throw new IllegalArgumentException("You try to calculate the angle between the X-Axis and a null-vector!");
		}
		float scalarProduct = this.scalarProduct(AVector2.X_AXIS);
		float result = AIMath.acos(scalarProduct / (1 * this.getLength2()));
		result = AIMath.normalizeAngle(result);
		if (this.y() < 0)
		{
			result = -result;
		}
		return result;
	}
	

	@Override
	public Vector2 addNew(IVector2 vector)
	{
		Vector2 result = new Vector2();
		result.x = this.x() + vector.x();
		result.y = this.y() + vector.y();
		
		return result;
	}
	

	@Override
	public Vector2 subtractNew(IVector2 vector)
	{
		Vector2 result = new Vector2();
		result.x = this.x() - vector.x();
		result.y = this.y() - vector.y();
		
		return result;
	}
	

	@Override
	public Vector2 multiplyNew(float factor)
	{
		Vector2 result = new Vector2();
		result.x = this.x() * factor;
		result.y = this.y() * factor;
		
		return result;
	}
	

	@Override
	public Vector2 scaleToNew(float newLength)
	{
		float oldLength = getLength2();
		if (oldLength != 0)
		{
			return multiplyNew(newLength / oldLength);
		} else
		{
			LOG.warn("You tried to scale a null-vector to a non-zero length! Vector stays unaffected.");
			return new Vector2(this);
		}
		
	}
	

	@Override
	public Vector2 turnNew(float angle)
	{
		float x2;
		float y2;
		
		x2 = this.x() * AIMath.cos(angle) - this.y() * AIMath.sin(angle);
		y2 = this.y() * AIMath.cos(angle) + this.x() * AIMath.sin(angle);
		
		return new Vector2(x2, y2);
	}
	

	@Override
	public float scalarProduct(IVector2 v)
	{
		return (this.x() * v.x() + this.y() * v.y());
	}
	

	@Override
	public Vector2 projectToNew(IVector2 v)
	{
		if (v.isZeroVector())
		{
			return new Vector2(AVector2.ZERO_VECTOR);
		}
		return v.multiplyNew(this.scalarProduct(v) / AIMath.square(v.getLength2()));
	}
	

	@Override
	public Vector2 turnToNew(float angle)
	{
		float len = this.getLength2();
		float yn = AIMath.sin(angle) * len;
		float xn = AIMath.cos(angle) * len;
		return new Vector2(xn, yn);
	}
	

	@Override
	public IVector2 roundNew(int digits)
	{
		final float newX;
		final float newY;
		
		if (digits == 0)
		{
			newX = Math.round(x());
			newY = Math.round(y());
		} else if (digits > 0)
		{
			final int f = 10 * digits;
			newX = Math.round(x() * f) / f;
			newY = Math.round(y() * f) / f;
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
		if(this.isZeroVector())
		{
			return new Vector2(this);
		}
		float length = this.getLength2();
		return new Vector2(x()/length, y()/length);
	}
	
	@Override
	public IVector2 getNormalVector()
	{
		Vector2 n = new Vector2(this.y(), -this.x());
		return n.normalize();
	}
	

	/**
	 * Overrides the 'equals()' Method.
	 * 
	 * @author Malte
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof IVector2)
		{
			IVector2 vec = (IVector2) obj;
			return vec.x() == this.x() && vec.y() == this.y();
		}
		
		return false;
	}
	

	@Override
	public boolean equals(IVector2 v)
	{
		return v.x() == this.x() && v.y() == this.y();
	}
	

	@Override
	public boolean equals(IVector2 vec, float tolerance)
	{
		return this.subtractNew(vec).getLength2() < tolerance;
	}
	

	/**
	 * Overrides the 'hashCode()' Method.
	 * 
	 * @author Malte
	 */
	@Override
	public int hashCode()
	{
		int hc = 17;
		int hashMultiplier = 59;
		
		hc = hc * hashMultiplier + new Float(x()).hashCode();
		hc = hc * hashMultiplier + new Float(y()).hashCode();
		
		return hc;
	}
	

	@Override
	public boolean isZeroVector()
	{
		if (this.x() == 0 && this.y() == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}
	

	@Override
	public boolean isVertical()
	{
		if ((this.x() == 0) && (this.y() != 0))
		{
			return true;
		} else
		{
			return false;
		}
	}
	

	@Override
	public boolean isHorizontal()
	{
		if ((this.y() == 0) && (this.x() != 0))
		{
			return true;
		} else
		{
			return false;
		}
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
		return x() + ";" + y();
	}	
}
