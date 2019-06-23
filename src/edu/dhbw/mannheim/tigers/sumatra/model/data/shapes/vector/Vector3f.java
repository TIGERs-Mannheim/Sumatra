/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.sleepycat.persist.model.Persistent;


/**
 * Similar to {@link Vector3}, but final/immutable!
 * 
 * @author Gero
 */
@Persistent
public class Vector3f extends AVector3
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private final float	x;
	private final float	y;
	private final float	z;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3f(final float x, final float y, final float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param xy
	 * @param z
	 */
	public Vector3f(final IVector2 xy, final float z)
	{
		x = xy.x();
		y = xy.y();
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public Vector3f(final IVector2 original)
	{
		this(original, 0);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public Vector3f(final IVector3 original)
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
	
	
	@Override
	public JSONObject toJSON()
	{
		Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
		jsonMapping.put("x", x);
		jsonMapping.put("y", y);
		jsonMapping.put("z", z);
		return new JSONObject(jsonMapping);
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(x);
		numbers.add(y);
		numbers.add(z);
		return numbers;
	}
	
	
	@Override
	public boolean isFinite()
	{
		return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z);
	}
}
