/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;


/**
 * Similar to {@link Vector2}, but final/immutable!
 * 
 * @author Gero
 * 
 */
public class Vector2f extends AVector2 implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= 5154123852292742379L;
	
	public static final Vector2f	ZERO					= new Vector2f(0, 0);
	
	public final float				x;
	public final float				y;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
		this.x = xy.x();
		this.y = xy.y();
	}
	
	/**
	 * Creates a final vector with (0, 0)
	 * 
	 */
	public Vector2f()
	{
		this.x = 0;
		this.y = 0;
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
