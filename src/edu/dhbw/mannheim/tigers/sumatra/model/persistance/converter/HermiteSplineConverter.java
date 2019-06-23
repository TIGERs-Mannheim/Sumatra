/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Convert old spline representation (a,b,c,d) to new (a[6])
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class HermiteSplineConverter implements Conversion
{
	
	/**  */
	private static final long	serialVersionUID	= -4007701186075874728L;
	private transient RawType	valuePointType;
	
	
	@Override
	public Object convert(Object fromValue)
	{
		RawObject spline = (RawObject) fromValue;
		Map<String, Object> values = spline.getValues();
		Float a = (Float) values.remove("a");
		Float b = (Float) values.remove("b");
		Float c = (Float) values.remove("c");
		Float d = (Float) values.remove("d");
		float[] na = new float[HermiteSpline.SPLINE_SIZE];
		na[0] = d;
		na[1] = c;
		na[2] = b;
		na[3] = a;
		values.put("a", na);
		return new RawObject(valuePointType, values, spline);
	}
	
	
	@Override
	public void initialize(EntityModel model)
	{
		valuePointType = model.getRawType(HermiteSpline.class.getName());
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
