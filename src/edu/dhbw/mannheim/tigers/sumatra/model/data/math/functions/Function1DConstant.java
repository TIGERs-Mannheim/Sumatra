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
 * One dimensional "function" of order zero. This is basically a constant :)
 * y = a
 * 
 * @author AndreR
 * 
 */
public class Function1DConstant implements IFunction1D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public float	a;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 * 
	 * a = 0.
	 */
	public Function1DConstant()
	{
		a = 0;
	}
	
	
	/**
	 * Constructor.
	 * @param a constant offset
	 */
	public Function1DConstant(float a)
	{
		this.a = a;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float eval(float x)
	{
		return a;
	}
	
	
	@Override
	public String getIdentifier()
	{
		return "const";
	}
	
	
	@Override
	public List<Float> getParameters()
	{
		List<Float> params = new ArrayList<Float>();
		params.add(a);
		
		return params;
	}
	
	
	@Override
	public void setParameters(List<Float> params)
	{
		a = params.get(0);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
