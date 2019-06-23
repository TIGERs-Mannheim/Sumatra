/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.export;

import java.util.List;


/**
 * Get a list of Number values ready to be exported as CSV
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface INumberListable
{
	/**
	 * @return
	 */
	List<Number> getNumberList();
}
