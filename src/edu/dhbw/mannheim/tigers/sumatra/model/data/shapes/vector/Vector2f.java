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

import com.sleepycat.persist.model.Persistent;


/**
 * Similar to {@link Vector2}, but final/immutable!
 * 
 * @author Gero
 * 
 */
@Persistent
public class Vector2f extends AVector2 implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 5154123852292742379L;
	
	private final float			x;
	private final float			y;
	
	
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
}
