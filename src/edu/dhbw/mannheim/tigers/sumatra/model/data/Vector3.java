/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * Simple data holder for position data
 * 
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see Vector2
 * @author Gero
 * 
 */
public class Vector3 extends Vector2 implements Serializable, IVector3
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6879424781834437894L;
	

	public float					z						= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Vector3()
	{
		super();
	}
	

	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3(float x, float y, float z)
	{
		super(x, y);
		this.z = z;
	}
	

	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public Vector3(IVector2 xy, float z)
	{
		set(xy, z);
	}
	

	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public Vector3(IVector3 original)
	{
		set(original);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void set(IVector2 original, float z)
	{
		super.set(original);
		this.z = z;
	}
	

	public void set(IVector3 original)
	{
		super.set(original);
		this.z = original.z();
	}
	

	public float getLength3()
	{
		return AIMath.sqrt(x * x + y * y + z * z);
	}
	

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
	

	@Override
	public String toString()
	{
		return "Vector3 (" + x + "," + y + "," + z + ")";
	}
}
