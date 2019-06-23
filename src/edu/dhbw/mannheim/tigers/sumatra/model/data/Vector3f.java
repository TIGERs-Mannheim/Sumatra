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
public class Vector3f extends Vector2f implements Serializable, IVector3
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 1696157382958854381L;
	
	
	public final float			z;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Vector3f(float x, float y, float z)
	{
		super(x, y);
		this.z = z;
	}
	

	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public Vector3f(IVector2 xy, float z)
	{
		super(xy);
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
		this(original, original.z());
	}

	
	/**
	  * Creates a final vector with (0,0,0) 
	  */
	public Vector3f()
	{
		super();
		this.z = 0;
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
}
