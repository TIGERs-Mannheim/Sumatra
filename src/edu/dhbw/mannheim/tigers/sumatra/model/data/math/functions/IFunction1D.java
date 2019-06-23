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

import java.util.List;


/**
 * Interface for one dimensional functions.
 * 
 * @author AndreR
 */
public interface IFunction1D
{
	/**
	 * Evaluate a function at point x.
	 * 
	 * @param x input
	 * @return function value
	 */
	public float eval(float x);
	
	
	/**
	 * Identifies function in saveable string.
	 * 
	 * @return
	 */
	public String getIdentifier();
	
	
	/**
	 * Get function parameters as list.
	 * Used for saving functions.
	 * 
	 * @return
	 */
	public List<Float> getParameters();
	
	
	/**
	 * Set function parameters.
	 * Used for creating functions from strings.
	 * 
	 * @param params
	 */
	public void setParameters(List<Float> params);
}
