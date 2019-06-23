/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.export;

import java.util.List;


/**
 * Get a list of Number values ready to be exported as CSV
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface INumberListable
{
	/**
	 * @return
	 */
	List<Number> getNumberList();
}
