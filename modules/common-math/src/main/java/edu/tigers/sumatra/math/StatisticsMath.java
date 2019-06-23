/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import java.util.ArrayList;
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
	 * @param values all values (will be sorted on return)
	 * @return the median value
	 */
	public static <T extends Number> double median(final List<T> values)
	{
		if (values.isEmpty())
		{
			return 0;
		}
		
		values.sort((v1, v2) -> Double.compare(v1.doubleValue(), v2.doubleValue()));
		
		int middle = values.size() / 2;
		if ((values.size() % 2) == 1)
		{
			return values.get(middle).doubleValue();
		}
		
		return (values.get(middle - 1).doubleValue() + values.get(middle).doubleValue()) / 2.0;
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
