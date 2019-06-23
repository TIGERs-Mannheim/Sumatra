/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.04.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * does a cubic spine interpolation of a series of points in order of input
 * @author DanielW
 * 
 */
public class XYSpline
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// private final Path path;
	private float[]	t;		// parameter
	private int			n;		// there are n+1 points, thus n parts to interpolate
										
	private Spline		xSpline;
	private Spline		ySpline;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param path list of points to be interpolated; order of input defines the trace
	 */
	public XYSpline(List<IVector2> path, IVector2 currentPosition)
	{
		// this.path = path;
		List<IVector2> cpath = new ArrayList<IVector2>();
		cpath.add(currentPosition);
		cpath.addAll(path);
		
		this.n = cpath.size() - 1;
		
		float[] x = new float[n + 1];
		float[] y = new float[n + 1];
		t = new float[n + 1];
		
		// get x and y
		for (int i = 0; i <= n; i++)
		{
			IVector2 p = cpath.get(i);
			x[i] = p.x();
			y[i] = p.y();
		}
		
		// get t
		t[0] = 0;
		for (int i = 1; i <= n; i++)
		{
			// TODO DanielW check if backwards is possible
			// backwards for(int i = n-1;i>=0;i--){
			// t_i[i] = t[i+1] path.path.get(i).subtractNew(path.path.get(i+1)).getLength2();
			
			// forward
			t[i] = t[i - 1] + cpath.get(i).subtractNew(cpath.get(i - 1)).getLength2();
			
		}
		
		xSpline = new Spline(t, x);
		ySpline = new Spline(t, y);
		

	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * get a point on the interpolated path
	 * @param tValue some value between 0 (beginning) and {@link #getMaxTValue()} (end)
	 * @return the point
	 */
	public IVector2 evaluateFunction(float tValue)
	{
		if (tValue > t[n] || tValue < 0)
		{
			throw new IllegalArgumentException("t parameter out of interpolation");
		}
		return new Vector2f(xSpline.evaluateFunction(tValue), ySpline.evaluateFunction(tValue));
	}
	

	/**
	 * get the tangetial vector of the interpolated path
	 * @param tValue some value between 0 (beginning) and {@link #getMaxTValue()} (end)
	 * @return
	 */
	public IVector2 getTangentialVector(float tValue)
	{
		return new Vector2f(xSpline.evaluateFirstDerivate(tValue), ySpline.evaluateFirstDerivate(tValue));
	}
	

	/**
	 * get the curvature at a given point on the interpolated curve
	 * @param tValue
	 * @return
	 */
	public float getCurvature(float tValue)
	{
		float xFirstDerivate = xSpline.evaluateFirstDerivate(tValue);
		float yFirstDerivate = ySpline.evaluateFirstDerivate(tValue);
		float xSecondDerivate = xSpline.evaluateSecondDerivate(tValue);
		float ySecondDerivate = ySpline.evaluateSecondDerivate(tValue);
		return (float) ((xFirstDerivate * ySecondDerivate - xSecondDerivate * yFirstDerivate) / Math.pow(
				(AIMath.square(xFirstDerivate) + AIMath.square(yFirstDerivate)), 3 / 2));
	}
	

	/**
	 * @return the t value at the end of the trace
	 */
	public float getMaxTValue()
	{
		return t[n];
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
