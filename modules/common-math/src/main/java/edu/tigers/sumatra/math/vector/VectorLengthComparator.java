package edu.tigers.sumatra.math.vector;

import java.util.Comparator;


/**
 * Comparator for the length of vectors
 */
public class VectorLengthComparator implements Comparator<IVector2>
{
	@Override
	public int compare(final IVector2 o1, final IVector2 o2)
	{
		double len1 = o1.getLength2();
		double len2 = o2.getLength2();
		return Double.compare(len1, len2);
	}
}
