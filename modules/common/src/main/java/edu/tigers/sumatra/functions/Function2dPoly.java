/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.g3force.s2vconverter.String2ValueConverter;
import com.sleepycat.persist.model.Persistent;


/**
 * Two dimensional function
 * y=a[0]+a[1]*x+a[2]*y+a[3]*x*y+a[4]*x*x+a[5]*y*y
 * 
 * @author AndreR
 */
@Persistent
public class Function2dPoly implements IFunction1D
{
	static
	{
		String2ValueConverter.getDefault().addConverter(new FunctionConverter());
	}
	
	/** */
	private final double[] a;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@SuppressWarnings("unused")
	private Function2dPoly()
	{
		a = new double[0];
	}
	
	
	/**
	 * Create polynomial function.
	 * param a must be in the form: y=a[0]+a[1]*x+a[2]*y+a[3]*x*y+a[4]*x*x+a[5]*y*y
	 * 
	 * @param a
	 */
	public Function2dPoly(final double[] a)
	{
		this.a = Arrays.copyOf(a, a.length);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public double eval(final double... x)
	{
		double m[] = new double[] { 1, x[0], x[1], x[0] * x[1], x[0] * x[0], x[1] * x[1] };
		double result = 0;
		for (int i = 0; i < Math.min(a.length, m.length); i++)
		{
			result += a[i] * m[i];
		}
		return result;
	}
	
	
	@Override
	public List<Double> getParameters()
	{
		List<Double> params = new ArrayList<Double>();
		
		for (double f : a)
		{
			params.add(f);
		}
		return params;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Function2DPoly[");
		builder.append(a[0]);
		for (int i = 1; i < a.length; i++)
		{
			builder.append(',').append(a[i]);
		}
		builder.append("]");
		return builder.toString();
	}
	
	
	@Override
	public EFunction getIdentifier()
	{
		return EFunction.POLY_2D;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
