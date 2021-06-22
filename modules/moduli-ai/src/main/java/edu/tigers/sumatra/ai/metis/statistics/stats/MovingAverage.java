/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

/**
 * A moving average with type long.
 */
public class MovingAverage
{
	private double sum;
	private long n;


	public void add(double value)
	{
		sum += value;
		n++;
	}


	public Double getCombinedValue()
	{
		if (n == 0)
		{
			return 0.0;
		}
		return sum / n;
	}
}
