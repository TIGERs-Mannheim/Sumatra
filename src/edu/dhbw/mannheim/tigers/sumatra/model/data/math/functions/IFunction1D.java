/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.03.2013
 * Author(s): AndreR
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
	float eval(float... x);
	
	
	/**
	 * Get function parameters as list.
	 * Used for saving functions.
	 * 
	 * @return
	 */
	List<Float> getParameters();
	
	
	/**
	 * @return
	 */
	EFunction getIdentifier();
}
