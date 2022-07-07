/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;


@Value
public class Force
{
	IVector2 position;
	double sigma;
	double magnitude;
	double mean;
	boolean invert;
	DistanceFunction func;


	Force(IVector2 position, double magnitude, DistanceFunction func)
	{
		this.position = position;
		this.sigma = 0;
		this.magnitude = magnitude;
		this.mean = 0;
		this.invert = false;
		this.func = func;
	}


	Force(IVector2 position, double sigma, double magnitude)
	{
		this.position = position;
		this.sigma = sigma;
		this.magnitude = magnitude;
		this.mean = 0;
		this.invert = false;
		this.func = DistanceFunction.EXPONENTIAL;
	}


	Force(IVector2 position, double sigma, double magnitude, double mean, boolean invert)
	{
		this.position = position;
		this.sigma = sigma;
		this.magnitude = magnitude;
		this.mean = mean;
		this.invert = invert;
		this.func = DistanceFunction.EXPONENTIAL;
	}


	public static Force dummy()
	{
		return new Force(Geometry.getCenter(), 0, DistanceFunction.CONSTANT);
	}
}