/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Utility for statistics math
 *
 * @author nicolai.ommer
 */
public final class StatisticsMath
{

	@SuppressWarnings("unused")
	private StatisticsMath()
	{
	}


	/**
	 * Calculate mean value
	 *
	 * @param values all values
	 * @return the mean value of all values
	 */
	public static <T extends Number> double mean(final List<T> values)
	{
		double sum = 0;
		for (Number f : values)
		{
			sum += f.doubleValue();
		}
		return sum / values.size();
	}


	/**
	 * Calculate mean value.
	 *
	 * @param values all values
	 * @return the median value
	 */
	public static <T extends Number> double median(final List<T> values)
	{
		if (values.isEmpty())
		{
			return 0;
		}

		List<T> valuesSorted = values.stream().sorted(Comparator.comparingDouble(Number::doubleValue)).toList();

		int middle = valuesSorted.size() / 2;
		if ((valuesSorted.size() % 2) == 1)
		{
			return valuesSorted.get(middle).doubleValue();
		}

		return (valuesSorted.get(middle - 1).doubleValue() + valuesSorted.get(middle).doubleValue()) / 2.0;
	}


	/**
	 * Calculate variance
	 *
	 * @param values all values
	 * @return the variance of all values
	 */
	public static <T extends Number> double variance(final List<T> values)
	{
		double mu = mean(values);
		List<Number> val2 = new ArrayList<>(values.size());
		for (Number f : values)
		{
			double diff = f.doubleValue() - mu;
			val2.add(diff * diff);
		}
		return mean(val2);
	}


	/**
	 * Calculate standard deviation
	 *
	 * @param values all values
	 * @return the standard deviation of all values
	 */
	public static <T extends Number> double std(final List<T> values)
	{
		return SumatraMath.sqrt(variance(values));
	}
}
