/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.functions;

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
	private final double[] a;
	
	
	@SuppressWarnings("unused")
	private Function1dPoly()
	{
		a = new double[0];
	}
	
	
	/**
	 * Create polynomial function.
	 * param a must be in the form: y=a[0]+a[1]*x+a[2]*x*x+...
	 * Consider using one of the static constructors for functions of low degree.
	 * 
	 * @param a
	 */
	public Function1dPoly(final double[] a)
	{
		this.a = Arrays.copyOf(a, a.length);
	}
	
	
	/**
	 * Constant function y=a
	 * 
	 * @param a
	 * @return
	 */
	public static IFunction1D constant(final double a)
	{
		return new Function1dPoly(new double[] { a });
	}
	
	
	/**
	 * Linear function y=a+bx
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static IFunction1D linear(final double a, final double b)
	{
		return new Function1dPoly(new double[] { a, b });
	}
	
	
	/**
	 * Quadratic function y=a+bx+cx^2
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static IFunction1D quadratic(final double a, final double b, final double c)
	{
		return new Function1dPoly(new double[] { a, b, c });
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public double eval(final double... x)
	{
		double xx = 1;
		double result = a[0];
		for (int i = 1; i < a.length; i++)
		{
			xx *= x[0];
			result += a[i] * xx;
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
