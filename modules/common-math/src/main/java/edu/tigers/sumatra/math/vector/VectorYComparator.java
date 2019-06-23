/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.Comparator;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorYComparator implements Comparator<IVector2>
{
	@Override
	public int compare(final IVector2 o1, final IVector2 o2)
	{
		return Double.compare(o1.y(), o2.y());
	}
}
