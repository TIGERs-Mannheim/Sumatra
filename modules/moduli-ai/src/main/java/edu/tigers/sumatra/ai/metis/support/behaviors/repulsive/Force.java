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
	EDistanceFunction func;


	Force(IVector2 position, double magnitude, EDistanceFunction func)
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
		this.func = EDistanceFunction.EXPONENTIAL;
	}


	Force(IVector2 position, double sigma, double magnitude, double mean, boolean invert)
	{
		this.position = position;
		this.sigma = sigma;
		this.magnitude = magnitude;
		this.mean = mean;
		this.invert = invert;
		this.func = EDistanceFunction.EXPONENTIAL;
	}


	public static Force dummy()
	{
		return new Force(Geometry.getCenter(), 0, EDistanceFunction.CONSTANT);
	}
}