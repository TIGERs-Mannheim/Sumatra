/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 6, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.functions;

import java.util.Arrays;
import java.util.List;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class NormalizerFunctionWrapper implements IFunction1D
{
	private final IFunction1D	function;
	private final double[]		means;
	private final double[]		std;
	
	
	/**
	 */
	@SuppressWarnings("unused")
	private NormalizerFunctionWrapper()
	{
		function = null;
		means = new double[0];
		std = new double[0];
	}
	
	
	/**
	 * @param function
	 * @param means
	 * @param std
	 */
	public NormalizerFunctionWrapper(final IFunction1D function, final double[] means, final double[] std)
	{
		this.function = function;
		this.means = Arrays.copyOf(means, means.length);
		this.std = Arrays.copyOf(std, std.length);
		assert means.length == std.length;
	}
	
	
	@Override
	public double eval(final double... x)
	{
		assert x.length == means.length;
		double[] dx = new double[x.length];
		for (int i = 0; i < x.length; i++)
		{
			dx[i] = (x[i] - means[i]) / std[i];
		}
		
		return function.eval(dx);
	}
	
	
	@Override
	public List<Double> getParameters()
	{
		List<Double> params = function.getParameters();
		for (double mean : means)
		{
			params.add(mean);
		}
		for (double element : std)
		{
			params.add(element);
		}
		return params;
	}
	
	
	@Override
	public EFunction getIdentifier()
	{
		return function.getIdentifier();
	}
	
}
