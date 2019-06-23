/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis;

import java.util.List;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IMetisHandler
{
	/**
	 * @param calculators
	 */
	void setActiveCalculators(List<ECalculator> calculators);
}
