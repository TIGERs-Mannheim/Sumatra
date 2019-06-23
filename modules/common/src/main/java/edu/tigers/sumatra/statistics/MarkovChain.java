/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statistics;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;


/**
 * This class is the implementation of a statistical Markov Chain
 * It can be instantiated for a specific class that can have different states
 * The initial intention for usages is with enums
 * 
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * @param <MappedClass> This describes the type of the class to be mapped
 */
@Persistent
public class MarkovChain<MappedClass>
{
	private Map<MappedClass, MarkovChainEntry<MappedClass>> containedStates = new HashMap<>();
	
	
	/**
	 * This function will increase the transitions for states in the markov chain
	 * 
	 * @param startState
	 * @param goalState
	 */
	public void increaseCountTransitions(final MappedClass startState, final MappedClass goalState)
	{
		MarkovChainEntry<MappedClass> entryToIncrease = containedStates.get(startState);
		
		if (entryToIncrease != null)
		{
			Integer countTransitions = entryToIncrease.absoluteTransitions.get(goalState);
			
			if (countTransitions != null)
			{
				entryToIncrease.absoluteTransitions.put(goalState, countTransitions + 1);
			} else
			{
				entryToIncrease.absoluteTransitions.put(goalState, 1);
			}
			
			entryToIncrease.transitions++;
		} else
		{
			MarkovChainEntry<MappedClass> newEntry = new MarkovChainEntry<MappedClass>();
			
			newEntry.absoluteTransitions.put(goalState, 1);
			
			containedStates.put(startState, newEntry);
		}
	}
	
	
	/**
	 * Get the absolute count of transitions between two states
	 * 
	 * @param startState
	 * @param goalState
	 * @return
	 */
	public int getAbsoluteCountTransitions(final MappedClass startState, final MappedClass goalState)
	{
		MarkovChainEntry<MappedClass> startEntry = containedStates.get(startState);
		
		if (startEntry != null)
		{
			Integer transitions = startEntry.absoluteTransitions.get(goalState);
			
			if (transitions != null)
			{
				return transitions;
			}
		}
		
		return 0;
	}
	
	
	/**
	 * returns the statistical relative count of transitions
	 * this value also describes the probabilities for a state change
	 * 
	 * @param startState
	 * @param goalState
	 * @return
	 */
	public float getRelativeCountTransitions(final MappedClass startState, final MappedClass goalState)
	{
		int absoluteTransitions = getAbsoluteCountTransitions(startState, goalState);
		
		MarkovChainEntry<MappedClass> startEntry = containedStates.get(startState);
		
		if (startEntry != null)
		{
			return (float) absoluteTransitions / startEntry.transitions;
		}
		
		return 0;
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param <M> This is the class that should be mapped by the markov chain. It represents the object from which
	 *           the transistions happen.
	 */
	@Persistent
	private class MarkovChainEntry<M>
	{
		/** This is the count for absolute transitions happen to a given state */
		Map<M, Integer> absoluteTransitions = new HashMap<>();
		
		/** This is the count of absolute transitions */
		int transitions = 1;
	}
}
