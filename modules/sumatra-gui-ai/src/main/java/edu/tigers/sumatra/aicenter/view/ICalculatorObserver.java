/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import edu.tigers.sumatra.ai.metis.ECalculator;


/**
 * Observer interface for calculator panel
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface ICalculatorObserver
{
	/**
	 * @param eCalc
	 * @param active
	 */
	void onCalculatorStateChanged(ECalculator eCalc, boolean active);
}
