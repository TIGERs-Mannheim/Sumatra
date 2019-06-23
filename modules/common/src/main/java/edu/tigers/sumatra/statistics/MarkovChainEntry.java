/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <MappedClass> This is the class that should be mapped by the markov chain. It represents the object from which
 *           the transistions happen.
 */
@Persistent
public class MarkovChainEntry<MappedClass>
{
	/** This is the count for absolute transitions happen to a given state */
	public Map<MappedClass, Integer>	absoluteTransitions	= new HashMap<>();
	
	/** This is the count of absolute transitions */
	public int								transitions				= 1;
}
