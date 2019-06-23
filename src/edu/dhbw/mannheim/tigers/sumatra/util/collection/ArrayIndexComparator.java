/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 31, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;

import java.util.Comparator;


/**
 * Comparator that sorts a float array, but returns the indices of the values
 * Example Usage:
 * ArrayIndexComparator comparator = new ArrayIndexComparator(dstArray);
 * Integer[] indexes = comparator.createIndexArray();
 * Arrays.sort(indexes, comparator);
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ArrayIndexComparator implements Comparator<Integer>
{
	private final float[]	array;
	
	
	private ArrayIndexComparator(final float[] array)
	{
		this.array = array;
	}
	
	
	/**
	 * @return
	 */
	public Integer[] createIndexArray()
	{
		Integer[] indexes = new Integer[array.length];
		for (int i = 0; i < array.length; i++)
		{
			indexes[i] = i; // Autoboxing
		}
		return indexes;
	}
	
	
	@Override
	public int compare(final Integer index1, final Integer index2)
	{
		// Autounbox from Integer to int to use as array indexes
		return Float.compare(array[index1], array[index2]);
	}
}
