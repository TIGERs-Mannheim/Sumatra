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
 * @param <ResultType>
 * @param <SampleType>
 */
public abstract class ADataStatistics<ResultType, SampleType>
{
	private final List<SampleType>	samples;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param samples
	 */
	public ADataStatistics(final List<SampleType> samples)
	{
		this.samples = samples;
	}
	
	
	/**
	 * @return
	 */
	public ResultType getAverage()
	{
		return getAverage(samples);
	}
	
	
	/**
	 * @return
	 */
	public ResultType getStandardDeviation()
	{
		return getStandardDeviation(samples);
	}
	
	
	/**
	 * @return
	 */
	public ResultType getMaximas()
	{
		return getMaximas(samples);
	}
	
	
	/**
	 * @return
	 */
	public ResultType getMinimas()
	{
		return getMinimas(samples);
	}
	
	
	/**
	 * @return
	 */
	public ResultType getRange()
	{
		return getRange(samples);
	}
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract ResultType getAverage(List<SampleType> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract ResultType getStandardDeviation(List<SampleType> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract ResultType getMaximas(List<SampleType> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract ResultType getMinimas(List<SampleType> samples);
	
	
	/**
	 * @param samples
	 * @return
	 */
	protected abstract ResultType getRange(List<SampleType> samples);
}
