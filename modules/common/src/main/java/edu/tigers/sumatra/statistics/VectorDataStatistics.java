/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics;

import java.util.List;

import edu.tigers.sumatra.math.vector.IVector;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorDataStatistics extends ADataStatistics<IVector, IVector>
{
	private final int	dim;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param samples
	 * @param dim
	 */
	public VectorDataStatistics(final List<IVector> samples, final int dim)
	{
		super(samples);
		this.dim = dim;
	}
	
	
	@Override
	protected IVector getAverage(final List<IVector> samples)
	{
		return samples.stream().collect(new CollectorVectorAvg(dim));
	}
	
	
	@Override
	protected IVector getStandardDeviation(final List<IVector> samples)
	{
		return samples.stream().collect(new CollectorVectorStd(getAverage(samples)));
	}
	
	
	@Override
	protected IVector getMaximas(final List<IVector> samples)
	{
		return samples.stream().collect(new CollectorVectorMax(dim));
	}
	
	
	@Override
	protected IVector getMinimas(final List<IVector> samples)
	{
		return samples.stream().collect(new CollectorVectorMin(dim));
	}
	
	
	@Override
	protected IVector getRange(final List<IVector> samples)
	{
		return getMaximas(samples).subtractNew(getMinimas(samples)).absNew();
	}
}
