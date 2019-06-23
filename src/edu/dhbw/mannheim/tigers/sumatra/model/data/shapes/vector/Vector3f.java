/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

import javax.persistence.Embeddable;


/**
 * Similar to {@link Vector3}, but final/immutable!
 * 
 * @author Gero
 * 
 */
@Embeddable
public class Vector3f extends AVector3
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** not final for ObjectDB */
	private float	x;
	/** not final for ObjectDB */
	private float	y;
	/** not final for ObjectDB */
	private float	z;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param xy
	 * @param z
	 */
	public Vector3f(IVector2 xy, float z)
	{
		x = xy.x();
		y = xy.y();
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public Vector3f(IVector2 original)
	{
		this(original, 0);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public Vector3f(IVector3 original)
	{
		x = original.x();
		y = original.y();
		z = original.z();
	}
	
	
	/**
	 * Creates a final vector with (0,0,0)
	 */
	public Vector3f()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float x()
	{
		return x;
	}
	
	
	@Override
	public float y()
	{
		return y;
	}
	
	
	@Override
	public float z()
	{
		return z;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param x the x to set
	 */
	protected void setX(float x)
	{
		this.x = x;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param y the y to set
	 */
	protected void setY(float y)
	{
		this.y = y;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param z the z to set
	 */
	protected void setZ(float z)
	{
		this.z = z;
	}
}
