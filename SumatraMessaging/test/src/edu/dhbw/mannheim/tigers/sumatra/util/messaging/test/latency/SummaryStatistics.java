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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


/**
 * Merges different statistics.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class SummaryStatistics extends AStats
{
	private final List<DescriptiveStatistics>	singleStats;
	
	
	/**
	 * @param numOfMessages
	 * @param numOfRuns
	 */
	public SummaryStatistics(int numOfMessages, int numOfRuns)
	{
		super(numOfMessages * numOfRuns);
		singleStats = new ArrayList<DescriptiveStatistics>(numOfRuns);
	}
	
	
	/**
	 * @param single
	 */
	public void addStats(DescriptiveStatistics single)
	{
		singleStats.add(single);
	}
	
	
	private void merge()
	{
		DescriptiveStatistics stats = getStats();
		for (DescriptiveStatistics single : singleStats)
		{
			for (double value : single.getValues())
			{
				stats.addValue(value);
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		merge();
		return super.toString();
	}
}
