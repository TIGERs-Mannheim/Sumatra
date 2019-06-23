/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sleepycat.persist.model.Persistent;


/**
 * One dimensional function
 * 
 * @author AndreR
 */
@Persistent
public class Function1dPoly implements IFunction1D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	private final float[]	a;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@SuppressWarnings("unused")
	private Function1dPoly()
	{
		a = new float[0];
	}
	
	
	/**
	 * Create polynomial function.
	 * param a must be in the form: y=a[0]+a[1]*x+a[2]*x*x+...
	 * Consider using one of the static constructors for functions of low degree.
	 * 
	 * @param a
	 */
	public Function1dPoly(final float[] a)
	{
		this.a = Arrays.copyOf(a, a.length);
	}
	
	
	/**
	 * Constant function y=a
	 * 
	 * @param a
	 * @return
	 */
	public static IFunction1D constant(final float a)
	{
		return new Function1dPoly(new float[] { a });
	}
	
	
	/**
	 * Linear function y=a+bx
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static IFunction1D linear(final float a, final float b)
	{
		return new Function1dPoly(new float[] { a, b });
	}
	
	
	/**
	 * Quadratic function y=a+bx+cx^2
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static IFunction1D quadratic(final float a, final float b, final float c)
	{
		return new Function1dPoly(new float[] { a, b, c });
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float eval(final float... x)
	{
		float xx = 1;
		float result = a[0];
		for (int i = 1; i < a.length; i++)
		{
			xx *= x[0];
			result += a[i] * xx;
		}
		return result;
	}
	
	
	@Override
	public List<Float> getParameters()
	{
		List<Float> params = new ArrayList<Float>();
		
		for (float f : a)
		{
			params.add(f);
		}
		return params;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Function1DPoly[");
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
		return EFunction.POLY_1D;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
