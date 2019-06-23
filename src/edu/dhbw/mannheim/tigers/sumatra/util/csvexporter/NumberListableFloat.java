/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 24, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.csvexporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class NumberListableFloat implements INumberListable
{
	private final List<Number>	numbers;
	
	
	/**
	 * @param floats
	 */
	public NumberListableFloat(final Float... floats)
	{
		numbers = new ArrayList<>(floats.length);
		numbers.addAll(Arrays.asList(floats));
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		return numbers;
	}
}
