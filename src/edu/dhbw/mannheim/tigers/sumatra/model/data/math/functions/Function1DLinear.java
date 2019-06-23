/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions;

import java.util.ArrayList;
import java.util.List;


/**
 * One dimensional function of first order.
 * y = a*x + b
 * 
 * @author AndreR
 * 
 */
public class Function1DLinear implements IFunction1D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public float	a;
	/** */
	public float	b;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * a = b = 0;
	 */
	public Function1DLinear()
	{
		a = 0;
		b = 0;
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param a a*x
	 * @param b constant offset
	 */
	public Function1DLinear(float a, float b)
	{
		this.a = a;
		this.b = b;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float eval(float x)
	{
		return (a * x) + b;
	}
	
	
	@Override
	public String getIdentifier()
	{
		return "linear";
	}
	
	
	@Override
	public List<Float> getParameters()
	{
		List<Float> params = new ArrayList<Float>();
		params.add(a);
		params.add(b);
		
		return params;
	}
	
	
	@Override
	public void setParameters(List<Float> params)
	{
		a = params.get(0);
		b = params.get(1);
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Function1DLinear [a=");
		builder.append(a);
		builder.append(", b=");
		builder.append(b);
		builder.append("]");
		return builder.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
