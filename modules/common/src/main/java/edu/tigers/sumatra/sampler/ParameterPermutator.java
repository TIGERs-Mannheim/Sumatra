/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sampler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Permute multiple parameter sets within a range and with a step size
 */
public class ParameterPermutator
{
	private final Map<String, ParameterSet> parameterSets = new LinkedHashMap<>();
	private boolean first = true;
	
	
	public void add(String id, double min, double max, double step)
	{
		parameterSets.put(id, new ParameterSet(min, max, step));
	}
	
	
	public Map<String, Double> next()
	{
		if (first)
		{
			first = false;
		} else
		{
			update();
		}
		return current();
	}
	
	
	public Map<String, Double> current()
	{
		return parameterSets.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().current));
	}
	
	
	private void update()
	{
		for (ParameterSet set : parameterSets.values())
		{
			set.next();
			if (!set.overflow)
			{
				break;
			}
		}
	}
	
	
	private static class ParameterSet
	{
		double min;
		double max;
		double step;
		double current;
		boolean overflow;
		
		
		public ParameterSet(final double min, final double max, final double step)
		{
			this.min = min;
			this.max = max;
			this.step = step;
			current = min;
			overflow = false;
		}
		
		
		void next()
		{
			current += step;
			if (current > max)
			{
				current = min;
				overflow = true;
			} else
			{
				overflow = false;
			}
		}
	}
}
