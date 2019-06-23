/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.08.2012
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector3} and
 * {@link Vector3f}
 * 
 * @see Vector3
 * @see Vector3f
 * 
 * @author AndreR
 */
@Persistent
public abstract class AVector3 implements IVector3
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** Vector3f(1,0,0) */
	public static final Vector3f	X_AXIS		= new Vector3f(1, 0, 0);
	/** Vector3f(0,1,0) */
	public static final Vector3f	Y_AXIS		= new Vector3f(0, 1, 0);
	/** Vector3f(0,0,1) */
	public static final Vector3f	Z_AXIS		= new Vector3f(0, 0, 1);
	/** Vector3f(0,0,0) */
	public static final Vector3f	ZERO_VECTOR	= new Vector3f(0, 0, 0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	AVector3()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public Vector3 addNew(IVector3 vector)
	{
		Vector3 result = new Vector3(this);
		if (vector != null)
		{
			result.add(vector);
		} else
		{
			throw new NullPointerException("Input vector is null");
		}
		
		return result;
	}
	
	
	@Override
	public Vector3 subtractNew(IVector3 vector)
	{
		Vector3 result = new Vector3(this);
		if (vector != null)
		{
			result.subtract(vector);
		} else
		{
			throw new NullPointerException("Input vector is null");
		}
		
		return result;
	}
	
	
	@Override
	public Vector3 multiplyNew(float f)
	{
		return new Vector3(x() * f, y() * f, z() * f);
	}
	
	
	@Override
	public Vector3 turnAroundZNew(float angle)
	{
		float x2;
		float y2;
		
		x2 = (x() * AngleMath.cos(angle)) - (y() * AngleMath.sin(angle));
		y2 = (y() * AngleMath.cos(angle)) + (x() * AngleMath.sin(angle));
		
		return new Vector3(x2, y2, z());
	}
	
	
	@Override
	public float getLength3()
	{
		return SumatraMath.sqrt((x() * x()) + (y() * y()) + (z() * z()));
	}
	
	
	@Override
	public float getLength2()
	{
		return SumatraMath.sqrt((x() * x()) + (y() * y()));
	}
	
	
	@Override
	public boolean equalsVector(IVector3 v)
	{
		return (v.x() == x()) && (v.y() == y()) && (v.z() == z());
	}
	
	
	@Override
	public boolean equalsVector(IVector3 vec, float tolerance)
	{
		return subtractNew(vec).getLength3() < tolerance;
	}
	
	
	@Override
	public boolean equals(IVector3 vec, float dx, float dy, float dz)
	{
		return (Math.abs(vec.x() - x()) <= dx) && (Math.abs(vec.y() - y()) <= dy) && (Math.abs(vec.z() - z()) <= dz);
	}
	
	
	@Override
	public float[] toArray()
	{
		return new float[] { x(), y(), z() };
	}
	
	
	@Override
	public double[] toDoubleArray()
	{
		return new double[] { x(), y(), z() };
	}
	
	
	@Override
	public Vector2 getXYVector()
	{
		return new Vector2(x(), y());
	}
	
	
	@Override
	public IVector3 mirrorXY()
	{
		return new Vector3f(-x(), -y(), z());
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
