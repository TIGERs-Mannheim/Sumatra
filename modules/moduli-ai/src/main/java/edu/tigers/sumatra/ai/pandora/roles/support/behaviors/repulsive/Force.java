/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class Force
{
	
	enum DistanceFunction
	{
		EXPONENTIAL,
		CONSTANT,
		LINEAR
	}
	
	IVector2 position = Vector2.zero();
	double sigma = 0;
	double magnitude = 0;
	double mean = 0;
	boolean invert = false;
	DistanceFunction func = DistanceFunction.EXPONENTIAL;
	
	
	Force(IVector2 position, double magnitude, DistanceFunction func)
	{
		this.position = position;
		this.magnitude = magnitude;
		this.func = func;
	}
	
	
	Force(IVector2 position, double sigma, double magnitude)
	{
		this.position = position;
		this.magnitude = magnitude;
		this.sigma = sigma;
	}
	
	
	Force(IVector2 position, double sigma, double magnitude, double mean, boolean invert)
	{
		this.position = position;
		this.magnitude = magnitude;
		this.sigma = sigma;
		this.mean = mean;
		this.invert = invert;
	}
	
	
	public static Force dummy()
	{
		return new Force(Geometry.getCenter(), 0, DistanceFunction.CONSTANT);
	}
}
