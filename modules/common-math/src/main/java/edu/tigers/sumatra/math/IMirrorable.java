/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

/**
 * Provide the capability to reflect this instance across the XY diagonal of a XY-plane.
 *
 * @param <T> the instance type to mirror
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IMirrorable<T>
{
	/**
	 * Reflect this instance across the XY-diagonal of the XY-plane
	 * 
	 * @return a mirrored instance
	 */
	T mirrored();
}
