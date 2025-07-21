/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification.movement;

import lombok.Getter;

import java.util.Arrays;


class Histogram
{
	private final double min;
	private final double binSize;

	@Getter
	private int samples = 0;
	private int[] bins;


	public Histogram(double min, double binSize, double max)
	{
		this.min = min;
		this.binSize = binSize;
		this.bins = new int[maxToBins(max)];
	}


	public void add(double value)
	{
		int bin = (int) Math.round((value - min) / binSize);
		if (bin < 0 || bin >= bins.length)
			return; // Sample outside of histogram range

		bins[bin]++;
		samples++;
	}


	public double getPercentile(double percentile)
	{
		final double percentileSamples = percentile * samples;
		double cumSamples = 0;

		// Sum all bins until the percentile sample amount is reached
		for (int i = 0; i < bins.length; i++)
		{
			cumSamples += bins[i];

			if (percentileSamples > cumSamples)
				continue;

			// Linear interpolation
			double bin = i * binSize + min;
			double percentilePreviousBin = (cumSamples - bins[i]) / samples;
			double percentileCurrentBin = cumSamples / samples;

			return bin + binSize
					* (percentile - percentilePreviousBin) / (percentileCurrentBin - percentilePreviousBin);
		}

		return (bins.length * binSize) + min;
	}


	public void setMax(double max)
	{
		int newLength = maxToBins(max);
		if (newLength == bins.length)
			return;

		for (int i = newLength; i < bins.length; i++)
			samples -= bins[i];

		bins = Arrays.copyOf(bins, newLength);
	}


	public void clear()
	{
		samples = 0;
		Arrays.fill(bins, 0);
	}


	private int maxToBins(double max)
	{
		return (int) Math.ceil((max - min) / binSize);
	}
}
