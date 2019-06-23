/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.04.2011
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;


/**
 * Just like {@link ValuePoint}, just that this is a set of Vector2f's with a value.
 * 
 * TODO Malte asks: Shouldn't this class be moved to model.data instead of model.impls.ai.dat.types?
 * @author GuntherB
 */
public class ValuePointPair implements Comparable<ValuePointPair>, IValueObject
{
	public final Vector2f	point1;
	public final Vector2f	point2;
	
	private float				value;
	
	
	public ValuePointPair(Vector2f point1, Vector2f point2)
	{
		this.point1 = point1;
		this.point2 = point2;
	}
	

	public ValuePointPair(ValuePoint point1, ValuePoint point2)
	{
		this.point1 = new Vector2f(point1);
		this.point2 = new Vector2f(point2);
	}
	

	/**
	 * TODO OliS: If compare is overwritten, equals (and thus hashcode) should be implemented, or write a comment (See
	 * {@link Comparable#compareTo(Object)}) (Gero)
	 */
	@Override
	public int compareTo(ValuePointPair otherPair)
	{
		if (otherPair.getValue() > this.getValue())
		{
			return 1;
		} else
		{
			return -1;
		}
	}
	

	@Override
	public float getValue()
	{
		return value;
	}
	

	@Override
	public void setValue(float value)
	{
		this.value = value;
	}
	

}
