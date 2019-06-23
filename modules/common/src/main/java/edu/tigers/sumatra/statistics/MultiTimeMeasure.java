/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;


/**
 * Measure multiple code parts and compare them.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MultiTimeMeasure
{
	private final Map<String, Double>	measurements	= new LinkedHashMap<>();
	private final Map<String, Long>		startTimes		= new HashMap<>();
	
	
	/**
	 * Start new measurement
	 * 
	 * @param id an identifier to distinguish between different measurement types
	 */
	public void startMeasurement(String id)
	{
		long now = System.nanoTime();
		startTimes.put(id, now);
		measurements.put(id, 0.0);
	}
	
	
	/**
	 * Stop current measurement
	 * 
	 * @param id an identifier to distinguish between different measurement types
	 */
	public void stopMeasurement(String id)
	{
		long now = System.nanoTime();
		Long tStart = startTimes.remove(id);
		Validate.notNull(tStart, "No Measurement running!");
		double diff = (now - tStart) / 1e9;
		measurements.put(id, diff);
	}
	
	
	public Map<String, Double> getMeasurements()
	{
		return Collections.unmodifiableMap(measurements);
	}
	
	
	/**
	 * @return current measurements as space separated string
	 */
	public String getMeasurementsSeconds()
	{
		return measurements.values().stream().map(Object::toString).collect(Collectors.joining(" "));
	}
	
	
	/**
	 * @return current measurements as space separated string
	 */
	public String getMeasurementsMilliseconds()
	{
		return measurements.values().stream().map(d -> Math.round(d * 1000)).map(Object::toString)
				.collect(Collectors.joining(" "));
	}
	
	
	/**
	 * Delete all measurements
	 */
	public void reset()
	{
		measurements.clear();
	}
	
	
	/**
	 * @return measurement identifiers as space separated string
	 */
	public String getHeader()
	{
		return measurements.keySet().stream().collect(Collectors.joining(" "));
	}
}
