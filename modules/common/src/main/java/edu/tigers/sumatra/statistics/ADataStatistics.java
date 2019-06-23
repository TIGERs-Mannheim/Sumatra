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


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <R> ResultType
 * @param <S> SampleType
 */
public abstract class ADataStatistics<R, S>
{
	private final List<S> samples;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param samples
	 */
	public ADataStatistics(final List<S> samples)
	{
		this.samples = samples;
	}
	
	
	/**
	 * @return
	 */
	public R getAverage()
	{
		return getAverage(samples);
	}
	
	
	/**
	 * @return
	 */
	public R getStandardDeviation()
	{
		return getStandardDeviation(samples);
	}
	
	
	/**
	 * @return
	 */
	public R getMaximas()
	{
		return getMaximas(samples);
	}
	
	
	/**
	 * @return
	 */
	public R getMinimas()
	{
		return getMinimas(samples);
	}
	
	
	/**
	 * @return
	 */
	public R getRange()
	{
		return getRange(samples);
	}
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract R getAverage(List<S> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract R getStandardDeviation(List<S> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract R getMaximas(List<S> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract R getMinimas(List<S> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract R getRange(List<S> samples);
}
