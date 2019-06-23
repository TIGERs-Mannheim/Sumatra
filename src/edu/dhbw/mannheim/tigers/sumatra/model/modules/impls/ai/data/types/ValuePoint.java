/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import java.io.Serializable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;


/**
 * This is a point with a specific value.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 *         TODO Malte asks: Shouldn't this class be moved to model.data instead of model.impls.ai.data.types?
 * 
 */
public class ValuePoint extends Vector2 implements Comparable<ValuePoint>, IValueObject, Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= -6549729498617629967L;
	
	private float					value;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ValuePoint(float x, float y)
	{
		super(x, y);
	}
	

	public ValuePoint(IVector2 vec, float value)
	{
		super(vec);
		this.value = value;
	}
	

	public ValuePoint(IVector2 vec)
	{
		super(vec);
	}
	

	public ValuePoint(float x, float y, float value)
	{
		super(x, y);
		this.value = value;
	}
	

	public ValuePoint(ValuePoint copy)
	{
		super(copy);
		this.value = copy.value;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float getValue()
	{
		return value;
	}
	

	public void setValue(float value)
	{
		this.value = value;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int compareTo(ValuePoint o)
	{
		if (o.getValue() >= this.value)
		{
			return 1;
		}
		
		return -1;
	}
}