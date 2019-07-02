package edu.tigers.sumatra.ai.metis.statistics;

/**
 * A moving average with type long.
 */
public class MovingAverage
{
	private double latestValue;
	private double sum;
	private long n;
	
	
	public void add(double value)
	{
		latestValue = value;
		sum += value;
		n++;
	}
	
	
	public Double getLatestValue()
	{
		return latestValue;
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
