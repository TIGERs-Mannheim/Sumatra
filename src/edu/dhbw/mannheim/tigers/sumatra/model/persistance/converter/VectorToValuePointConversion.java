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
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawType;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Converts {@link IVector2} to ValuePoint
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class VectorToValuePointConversion implements Conversion
{
	
	/**  */
	private static final long	serialVersionUID	= 5730156178369434945L;
	
	private transient RawType	valuePointType;
	
	
	@Override
	public Object convert(Object fromValue)
	{
		RawObject vector = (RawObject) fromValue;
		Map<String, Object> vectorValues = vector.getValues();
		vectorValues.put("value", Float.valueOf(0f));
		
		return new RawObject(valuePointType, vectorValues, vector);
	}
	
	
	@Override
	public void initialize(EntityModel model)
	{
		valuePointType = model.getRawType(ValuePoint.class.getName());
	}
	
	
	@Override
	public boolean equals(Object o)
	{
		return o instanceof IVector2;
	}
	
	
	@Override
	public int hashCode()
	{
		return 0;
	}
}
