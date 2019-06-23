/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.Comparator;


/**
 * Comparator for the length of vectors
 *
 * @author nicolai.ommer
 */
public class VectorLengthComparator implements Comparator<IVector2>
{
	@Override
	public int compare(final IVector2 o1, final IVector2 o2)
	{
		double len1 = o1.getLength2();
		double len2 = o2.getLength2();
		if (len1 < len2)
		{
			return -1;
		}
		if (len2 < len1)
		{
			return 1;
		}
		return 0;
	}
}
