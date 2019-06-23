/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.export.INumberListable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <ResultType>
 */
public abstract class ADataCollector<ResultType extends INumberListable>
{
	private final EDataCollector			type;
	private final Map<Long, ResultType>	samples	= new LinkedHashMap<>();
																
																
	protected ADataCollector(final EDataCollector type)
	{
		this.type = type;
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		samples.clear();
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
	}
	
	
	/**
	 * @return
	 */
	public synchronized List<ResultType> getSamples()
	{
		return new ArrayList<>(samples.values());
	}
	
	
	protected synchronized void addSample(final ResultType sample)
	{
		samples.put(System.currentTimeMillis(), sample);
	}
	
	
	/**
	 * @param filename
	 */
	public synchronized void exportSamples(final String filename)
	{
		CSVExporter exporter = new CSVExporter(filename, false);
		for (Map.Entry<Long, ResultType> entry : samples.entrySet())
		{
			Long t = entry.getKey();
			ResultType sample = entry.getValue();
			List<Number> numbers = sample.getNumberList();
			numbers.add(0, t);
			exporter.addValues(numbers);
		}
		exporter.close();
	}
	
	
	/**
	 * @return
	 */
	public final int getNumSamples()
	{
		return samples.size();
	}
	
	
	/**
	 * @return the type
	 */
	public final EDataCollector getType()
	{
		return type;
	}
}
