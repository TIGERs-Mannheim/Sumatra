/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.latency;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class AStats
{
	
	private DescriptiveStatistics	stats;
	private int							numOfMessages;
	private Map<Integer, Long>		values	= new HashMap<Integer, Long>();
	
	
	/**
	 * @param numOfMessages
	 */
	public AStats(int numOfMessages)
	{
		stats = new DescriptiveStatistics(numOfMessages);
		this.numOfMessages = numOfMessages;
	}
	
	
	/**
	 * @return
	 */
	public double getAriMean()
	{
		return stats.getMean();
	}
	
	
	/**
	 * @return
	 */
	public double getGeoMean()
	{
		return stats.getGeometricMean();
	}
	
	
	/**
	 * @return
	 */
	public double getMin()
	{
		return stats.getMin();
	}
	
	
	/**
	 * @return
	 */
	public double getMax()
	{
		return stats.getMax();
	}
	
	
	/**
	 * @return
	 */
	public double getStdDev()
	{
		return stats.getStandardDeviation();
	}
	
	
	/**
	 * @return
	 */
	public double getMedian()
	{
		return stats.getPercentile(50);
	}
	
	
	/**
	 * @return
	 */
	private long getLost()
	{
		return numOfMessages - stats.getN();
	}
	
	
	/**
	 * @return
	 */
	private double getKurtosis()
	{
		return stats.getKurtosis();
	}
	
	
	/**
	 * @return
	 */
	private double getSkewness()
	{
		return stats.getSkewness();
	}
	
	
	@Override
	public String toString()
	{
		DecimalFormat df = new DecimalFormat("###,###,###,##0.#");
		
		StringBuilder builder = new StringBuilder();
		builder.append("---------Statistics---------\n");
		builder.append(String.format("%10s: %s%n", "Requested", df.format(numOfMessages)));
		builder.append(String.format("%10s: %s%n", "Lost", df.format(getLost())));
		builder.append(String.format("%10s: %s%n", "Min", df.format(getMin())));
		builder.append(String.format("%10s: %s%n", "Median", df.format(getMedian())));
		builder.append(String.format("%10s: %s%n", "Max", df.format(getMax())));
		builder.append(String.format("%10s: %s%n", "AriAverage", df.format(getAriMean())));
		builder.append(String.format("%10s: %s%n", "GeoAverage", df.format(getGeoMean())));
		builder.append(String.format("%10s: %s%n", "Std Dev", df.format(getStdDev())));
		builder.append(String.format("%10s: %s%n", "Kurtosis", df.format(getKurtosis())));
		builder.append(String.format("%10s: %s%n", "Skewness", df.format(getSkewness())));
		return builder.toString();
	}
	
	
	/**
	 * @return the stats
	 */
	public DescriptiveStatistics getStats()
	{
		return stats;
	}
	
	
	/**
	 * @param id
	 * @param value
	 */
	public void putValue(int id, long value)
	{
		values.put(id, value);
	}
	
	
	/**
	 */
	public void calcStats()
	{
		for (int i = 0; i < numOfMessages; i++)
		{
			Long value = values.get(i);
			if (value != null)
			{
				stats.addValue(value);
				// System.out.printf("id: %d value: %d\n", i, value);
			}
		}
	}
}
