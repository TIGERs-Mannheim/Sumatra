/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.Comparator;


/**
 * Compare vectors by distance to a fixed point.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorDistanceComparator implements Comparator<IVector2>
{
	
	private final IVector2 toVector;
	
	
	/**
	 * @param toVector the vector to calculate the distance to
	 */
	public VectorDistanceComparator(final IVector2 toVector)
	{
		this.toVector = toVector;
	}
	
	
	@Override
	public int compare(final IVector2 o1, final IVector2 o2)
	{
		return Double.compare(o1.distanceTo(toVector), o2.distanceTo(toVector));
	}
}
