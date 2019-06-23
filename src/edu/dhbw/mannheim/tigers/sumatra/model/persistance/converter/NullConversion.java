/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance.converter;

import com.sleepycat.persist.evolve.Conversion;
import com.sleepycat.persist.model.EntityModel;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Converts {@link IVector2} to ValuePoint
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class NullConversion implements Conversion
{
	
	/**  */
	private static final long	serialVersionUID	= 5730156178369434945L;
	
	
	@Override
	public Object convert(final Object fromValue)
	{
		return null;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		return true;
	}
	
	
	@Override
	public int hashCode()
	{
		return 0;
	}
	
	
	@Override
	public void initialize(final EntityModel model)
	{
	}
}
