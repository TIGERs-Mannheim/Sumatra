/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * does a cubic spine interpolation
 * @see http://www.unibw.de/bauv1/lehre/Ingenieurmathematik/Skriptum/getFILE?fid=1985716&tid=Skriptum <br>
 *      for algorithm
 *      details
 * 
 * @author DanielW
 * 
 */
public class Spline
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	private int	n; // there are n+1 points, thus n parts to interpolate
						
	private float[]	x, y; // x, y values
			
	// parameters of cubic parts ax^3 + bx^2 + cx + d
	private float[]	a, b, c, d;
	
	private float[]	h;			// distance between points
											
	// equation system
	private float[]	e, u, k, r;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param xValues x values to be interpolated, <em>have to be in ascending order!</em>
	 * @param yValues y values to be interpolated
	 */
	public Spline(float[] xValues, float[] yValues)
	{
		if (xValues.length != yValues.length)
		{
			throw new IllegalArgumentException("there have to be as many x-values as y-values");
		}
		this.x = xValues;
		this.y = yValues;
		this.n = xValues.length - 1;
		
		h = new float[n];
		e = new float[n];
		u = new float[n];
		k = new float[n + 1];
		r = new float[n];
		
		a = new float[n];
		b = new float[n];
		c = new float[n];
		d = new float[n];
		
		interpolate();
		

	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * does the actual interpolating
	 */
	private void interpolate()
	{
		// calc h_i
		for (int i = 0; i <= n - 1; i++)
		{
			h[i] = x[i + 1] - x[i];
			e[i] = 6 / h[i] * (y[i + 1] - y[i]);
		}
		
		// forward inserting
		if (n > 1)
		{
			u[1] = 2 * (h[0] + h[1]);
			r[1] = e[1] - e[0];
		}
		for (int i = 2; i <= n - 1; i++)
		{
			u[i] = 2 * (h[i] + h[i - 1]) - h[i - 1] * h[i - 1] / u[i - 1];
			r[i] = (e[i] - e[i - 1]) - r[i - 1] * h[i - 1] / u[i - 1];
		}
		

		// backsubstitution
		k[n] = 0; // natural splines TODO DanielW better to set first derivates?
		for (int i = n - 1; i >= 1; i--)
		{
			k[i] = (r[i] - h[i] * k[i + 1]) / u[i];
		}
		k[0] = 0;
		

		// get parameters
		for (int i = 0; i < n; i++)
		{
			d[i] = y[i];
			c[i] = (y[i + 1] - y[i]) / h[i] - h[i] / 6 * (2 * k[i] + k[i + 1]);
			b[i] = k[i] / 2;
			a[i] = (k[i + 1] - k[i]) / (6 * h[i]);
		}
	}
	

	/**
	 * get a value of the interpoated function
	 * @param xValue some value between 0 and {@link #getMaxXValue()}
	 * @return the corresponding y value
	 */
	public float evaluateFunction(float xValue)
	{
		// find out wich part is to be evaluated
		int i = findPart(xValue);
		
		// calc y
		return a[i] * AIMath.cubic(xValue - x[i]) + b[i] * AIMath.square(xValue - x[i]) + c[i] * (xValue - x[i]) + d[i];
	}
	

	/**
	 * get a value of the first derivate
	 * @param xValue some value between 0 and {@link #getMaxXValue()}
	 * @return
	 */
	public float evaluateFirstDerivate(float xValue)
	{
		// find out wich part is to be evaluated
		int i = findPart(xValue);
		
		// calc derivate
		return 3 * a[i] * AIMath.square(xValue - x[i]) + 2 * b[i] * (xValue - x[i]) + c[i];
	}
	

	/**
	 * get a value of the second derivate
	 * @param xValue some value between 0 and {@link #getMaxXValue()}
	 * @return
	 */
	public float evaluateSecondDerivate(float xValue)
	{
		// find out wich part is to be evaluated
		int i = findPart(xValue);
		
		// calt second derivate
		return 6 * a[i] * (xValue - x[i]) + 2 * b[i];
	}
	

	/**
	 * @return the biggest possible x value
	 */
	public float getMaxXValue()
	{
		return x[n];
	}
	

	private int findPart(float xValue)
	{
		// TODO DanielW: optimise for sequential access
		for (int i = 0; i < n; i++)
		{
			if (xValue >= x[i] && xValue <= x[i + 1])
			{
				return i;
			}
		}
		throw new IllegalArgumentException("value outside interpolation");
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
