/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;


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
