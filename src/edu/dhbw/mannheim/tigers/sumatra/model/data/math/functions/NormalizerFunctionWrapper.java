/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 6, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions;

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
	private final float[]		means;
	private final float[]		std;
	
	
	/**
	 */
	@SuppressWarnings("unused")
	private NormalizerFunctionWrapper()
	{
		function = null;
		means = new float[0];
		std = new float[0];
	}
	
	
	/**
	 * @param function
	 * @param means
	 * @param std
	 */
	public NormalizerFunctionWrapper(final IFunction1D function, final float[] means, final float[] std)
	{
		this.function = function;
		this.means = Arrays.copyOf(means, means.length);
		this.std = Arrays.copyOf(std, std.length);
		assert means.length == std.length;
	}
	
	
	@Override
	public float eval(final float... x)
	{
		assert x.length == means.length;
		float[] dx = new float[x.length];
		for (int i = 0; i < x.length; i++)
		{
			dx[i] = (x[i] - means[i]) / std[i];
		}
		
		return function.eval(dx);
	}
	
	
	@Override
	public List<Float> getParameters()
	{
		List<Float> params = function.getParameters();
		for (float mean : means)
		{
			params.add(mean);
		}
		for (float element : std)
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
