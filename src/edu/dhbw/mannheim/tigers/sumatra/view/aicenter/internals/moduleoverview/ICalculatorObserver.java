/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.util.List;


/**
 * Observer interface for calculator panel
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface ICalculatorObserver
{
	
	/**
	 * @param values
	 */
	void selectedCalculatorsChanged(List<String> values);
	
}
