/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 6, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;


/**
 * @author "Lukas Magel"
 */
public interface IRefereeCalc
{
	
	/**
	 * @param frame
	 */
	void process(AutoRefFrame frame);
	
}
