/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static java.lang.Math.pow;

import java.util.function.Function;


/**
 * This class can contain a score function for a PassTarget
 */
public class WeightedScoreFunction implements Function<IPassTarget, Double>
{
	private double minValue;
	private double weight;
	private Function<IPassTarget, Double> function;
	
	
	WeightedScoreFunction(final Function<IPassTarget, Double> function, final double weight, final double minValue)
	{
		this.function = function;
		this.minValue = minValue;
		this.weight = weight;
	}
	
	
	WeightedScoreFunction(final Function<IPassTarget, Double> function, final double weight)
	{
		this(function, weight, 0.0);
	}
	
	
	@Override
	public Double apply(final IPassTarget passTarget)
	{
		double score = function.apply(passTarget);
		score = (score < 0.0) ? 0.0 : score;
		score = ((1 - minValue) * score) + minValue;
		return pow(score, weight);
	}
	
	
	public double getWeight()
	{
		return weight;
	}
}