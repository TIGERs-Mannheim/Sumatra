/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.filter;

/**
 * Make a class interpolatable.
 *
 * @param <T>
 */
public interface IInterpolatable<T>
{
	/**
	 * Interpolate between this and `other`, using `percentage` of `other`.
	 *
	 * @param other the data to interpolate with this
	 * @param percentage the relative part to take from `other`
	 * @return the interpolated data
	 */
	T interpolate(T other, double percentage);
}
