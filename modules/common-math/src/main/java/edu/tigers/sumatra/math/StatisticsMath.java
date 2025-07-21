/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;


/**
 * Utility for statistics math
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsMath
{
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


	public static <T> double mean(List<T> values, ToDoubleFunction<T> valueExtractor)
	{
		double sum = 0;
		for (T f : values)
		{
			sum += valueExtractor.applyAsDouble(f);
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


	public static <T> double variance(List<T> values, ToDoubleFunction<T> valueExtractor)
	{
		double mu = mean(values, valueExtractor);
		List<Number> val2 = new ArrayList<>(values.size());
		for (T f : values)
		{
			double diff = valueExtractor.applyAsDouble(f) - mu;
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


	public static <T> double std(List<T> values, ToDoubleFunction<T> valueExtractor)
	{
		return SumatraMath.sqrt(variance(values, valueExtractor));
	}


	/**
	 * Calculate signal-to-noise ratio
	 *
	 * @param values all values
	 * @return the snr of all values
	 */
	public static <T extends Number> double snr(final List<T> values)
	{
		double mu = mean(values);
		double std = std(values);
		return Math.abs(mu) / std;
	}


	public static <T> double snr(List<T> values, ToDoubleFunction<T> valueExtractor)
	{
		double mu = mean(values, valueExtractor);
		double std = std(values, valueExtractor);
		return Math.abs(mu) / std;
	}


	/**
	 * Given a list of probabilities describing if their respective events occurs, calculates the probability that any one of the events occurs:
	 * pAny = 1 - (1-p1) * (1-p2) * ...
	 *
	 * @param probabilities List of probabilities p1, p2, ... in the range of 0..1
	 * @return
	 */
	public static double anyOccurs(List<Double> probabilities)
	{
		return anyOccurs(probabilities.stream());
	}


	/**
	 * Given a list of probabilities describing if their respective events occurs, calculates the probability that any one of the events occurs:
	 * pAny = 1 - (1-p1) * (1-p2) * ...
	 *
	 * @param probabilities List of probabilities p1, p2, ... in the range of 0..1
	 * @return
	 */
	public static double anyOccurs(Stream<Double> probabilities)
	{
		// Using a computationally more stable way
		// pAny = 1 - (1-p1) * (1-p2) * ...
		// pAny = 1 - exp(log((1-p1) * (1-p2) * ...))
		// pAny = 1 - exp(log(1-p1) + log(1-p2) + ...)
		return 1.0 - Math.exp(probabilities.mapToDouble(t -> 1.0 - t).map(Math::log).sum());
	}
}
