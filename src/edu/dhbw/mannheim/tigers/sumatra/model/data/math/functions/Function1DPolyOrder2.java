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
 * One dimensional function of second order.
 * y = a*x� + b*x + c
 * 
 * @author AndreR
 * 
 */
public class Function1DPolyOrder2 implements IFunction1D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public float	a;
	/** */
	public float	b;
	/** */
	public float	c;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * a = b = c = 0.
	 */
	public Function1DPolyOrder2()
	{
		a = 0;
		b = 0;
		c = 0;
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param a a*x�
	 * @param b b*x
	 * @param c constant offset
	 */
	public Function1DPolyOrder2(float a, float b, float c)
	{
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float eval(float x)
	{
		return (a * x * x) + (b * x) + c;
	}
	
	
	@Override
	public String getIdentifier()
	{
		return "poly2";
	}
	
	
	@Override
	public List<Float> getParameters()
	{
		List<Float> params = new ArrayList<Float>();
		params.add(a);
		params.add(b);
		params.add(c);
		
		return params;
	}
	
	
	@Override
	public void setParameters(List<Float> params)
	{
		a = params.get(0);
		b = params.get(1);
		c = params.get(2);
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Function1DPolyOrder2 [a=");
		builder.append(a);
		builder.append(", b=");
		builder.append(b);
		builder.append(", c=");
		builder.append(c);
		builder.append("]");
		return builder.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
