/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance.converter;

import java.util.Map;

import com.sleepycat.persist.evolve.Conversion;
import com.sleepycat.persist.model.EntityModel;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Converts {@link IVector2} to ValuePoint
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class MapToEmptyConversion implements Conversion
{
	
	/**  */
	private static final long	serialVersionUID	= 5730156178369434945L;
	
	
	@Override
	public Object convert(Object fromValue)
	{
		return null;
	}
	
	
	@Override
	public boolean equals(Object o)
	{
		return o instanceof Map;
	}
	
	
	@Override
	public int hashCode()
	{
		return 0;
	}
	
	
	@Override
	public void initialize(EntityModel model)
	{
	}
}
