/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


/**
 * Utility class for stream-related helper methods
 */
public final class StreamUtil
{
	private StreamUtil()
	{
	}
	
	
	/**
	 * Create a stream of non repeating permutations from a given list.</br>
	 * The stream will contain lists with 2 elements each.
	 * 
	 * @param l the list
	 * @param <T> the type of the list
	 * @return (n! permutations, n=l.size())
	 */
	public static <T> Stream<List<T>> nonRepeatingPermutation2Fold(List<T> l)
	{
		Stream.Builder<List<T>> builder = Stream.builder();
		for (int i = 0; i < l.size() - 1; i++)
		{
			for (int j = i + 1; j < l.size(); j++)
			{
				builder.add(Arrays.asList(l.get(i), l.get(j)));
			}
		}
		return builder.build();
	}
	
	
	/**
	 * Create a stream of non repeating permutations from a given list.</br>
	 * The stream will contain lists with 3 elements each.
	 *
	 * @param l the list
	 * @param <T> the type of the list
	 * @return (n! permutations, n=l.size())
	 */
	public static <T> Stream<List<T>> nonRepeatingPermutation3Fold(List<T> l)
	{
		Stream.Builder<List<T>> builder = Stream.builder();
		for (int i = 0; i < l.size() - 2; i++)
		{
			for (int j = i + 1; j < l.size() - 1; j++)
			{
				for (int k = j + 1; k < l.size(); k++)
				{
					builder.add(Arrays.asList(l.get(i), l.get(j), l.get(k)));
				}
			}
		}
		return builder.build();
	}
}
