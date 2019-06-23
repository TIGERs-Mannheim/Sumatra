/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statistics;

import java.util.List;

import edu.tigers.sumatra.math.vector.IVectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorDataStatistics extends ADataStatistics<IVectorN, IVectorN>
{
	private final int	dim;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param samples
	 * @param dim
	 */
	public VectorDataStatistics(final List<IVectorN> samples, final int dim)
	{
		super(samples);
		this.dim = dim;
	}
	
	
	@Override
	protected IVectorN getAverage(final List<IVectorN> samples)
	{
		return samples.stream().collect(new CollectorVectorAvg(dim));
	}
	
	
	@Override
	protected IVectorN getStandardDeviation(final List<IVectorN> samples)
	{
		return samples.stream().collect(new CollectorVectorStd(getAverage(samples)));
	}
	
	
	@Override
	protected IVectorN getMaximas(final List<IVectorN> samples)
	{
		return samples.stream().collect(new CollectorVectorMax(dim));
	}
	
	
	@Override
	protected IVectorN getMinimas(final List<IVectorN> samples)
	{
		return samples.stream().collect(new CollectorVectorMin(dim));
	}
	
	
	@Override
	protected IVectorN getRange(final List<IVectorN> samples)
	{
		return getMaximas(samples).subtractNew(getMinimas(samples)).absNew();
	}
}
