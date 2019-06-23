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

import java.io.Serializable;

import javax.persistence.Embeddable;


/**
 * Similar to {@link Vector2}, but final/immutable!
 * 
 * @author Gero
 * 
 */
@Embeddable
public class Vector2f extends AVector2 implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= 5154123852292742379L;
	
	/** */
	public static final Vector2f	ZERO					= new Vector2f(0, 0);
	
	/** not final for ObjectDB */
	private float						x;
	/** not final for ObjectDB */
	private float						y;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public Vector2f(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param xy
	 */
	public Vector2f(IVector2 xy)
	{
		x = xy.x();
		y = xy.y();
	}
	
	
	/**
	 * Creates a final vector with (0, 0)
	 * 
	 */
	public Vector2f()
	{
		x = 0;
		y = 0;
	}
	
	
	@Override
	public boolean similar(IVector2 vec, float treshold)
	{
		final IVector2 newVec = subtractNew(vec);
		return (Math.abs(newVec.x()) < treshold) && (Math.abs(newVec.y()) < treshold);
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
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param x the x to set
	 */
	@Deprecated
	protected void setX(float x)
	{
		this.x = x;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param y the y to set
	 */
	@Deprecated
	protected void setY(float y)
	{
		this.y = y;
	}
}
