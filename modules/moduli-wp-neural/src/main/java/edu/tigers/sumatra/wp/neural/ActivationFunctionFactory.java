/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationTANH;


/**
 * Factory class to generate the Activation-Function that should be used in the neural
 * Network.
 * 
 * @author KaiE
 */
public class ActivationFunctionFactory
{
	/**
	 * creates a new Object of the Activation-function
	 * 
	 * @return
	 *         returns the activationFunction for the NN
	 */
	public static ActivationFunction create()
	{
		return new ActivationTANH();
	}
	
	
	/**
	 * Getter for the upper bound
	 * 
	 * @return
	 *         the upper bound of the function
	 */
	public static double getNormalizedHigh()
	{
		return 1;
	}
	
	
	/**
	 * getter for the lower bound
	 * 
	 * @return
	 *         the lower bound of the function
	 */
	public static double getNormalizedLow()
	{
		return -1;
	}
}
