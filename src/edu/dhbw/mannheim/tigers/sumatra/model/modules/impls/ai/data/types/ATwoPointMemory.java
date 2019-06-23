/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.01.2011
 * Author(s): Vendetta
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import java.util.ArrayList;
import java.util.Collections;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;


/**
 * This works just as a PointMemory, just with two random points that need to be good as
 * a pair, and can be seen as such : a pair. For more documentary and basic understanding please
 * see APointMemory.
 * 
 * Value of the pair is saved in the first Points value-variable
 * 
 * @author GuntherB
 * 
 */

public abstract class ATwoPointMemory
{
	
	public final int						MEMORYSIZE;
	public final int						TRIES_PER_CYCLE;
	
	public ArrayList<ValuePointPair>	bestPairs;
	
	
	/**
	 * Extends a memory that will generate good points depending on your algorithms in generateNewPoint and evaluatePoint
	 * @param memorySize
	 * @param triesPerCycle
	 */
	public ATwoPointMemory(int memorySize, int triesPerCycle, AIInfoFrame curFrame)
	{
		MEMORYSIZE = memorySize;
		TRIES_PER_CYCLE = triesPerCycle;
		

		bestPairs = new ArrayList<ValuePointPair>();
		
		for (int c = 0; c < MEMORYSIZE; c++)
		{
			bestPairs.add(generateNewPair(curFrame));
		}
		

	}
	

	/**
	 * This is the only method a calling class will actively call. A new set of random points will be generated and the
	 * best will be returned
	 * 
	 * @param currentFrame
	 * @return
	 */
	public ValuePointPair generateBestPoints(AIInfoFrame currentFrame)
	{
		// points from last cycle
		for (ValuePointPair pair : bestPairs)
		{
			pair.setValue(evaluatePair(pair, currentFrame));
		}
		
		Collections.sort(bestPairs);
		
		// new points
		for (int counter = 0; counter < TRIES_PER_CYCLE; counter++)
		{
			ValuePointPair newPair = generateNewPair(currentFrame);
			newPair.setValue(evaluatePair(newPair, currentFrame));
			
			int index = 0;
			for (ValuePointPair pair : bestPairs)
			{
				if (newPair.getValue() > pair.getValue())
				{
					break;
				}
				index++;
			}
			
			// insert at given index and delete last element
			bestPairs.add(index, newPair);
			bestPairs.remove(bestPairs.size() - 1);
			
		}
		
		// // debug!
		//
		// System.out.println("------ATwoPointMemory--------------------");
		// for (ValuePointPair p : bestPairs)
		// {
		// System.out.println(p.point1.toString());
		// }
		//
		return bestPairs.get(0);
	}
	

	/** How a point will be generated */
	private ValuePointPair generateNewPair(AIInfoFrame currentFrame)
	{
		return new ValuePointPair(generateFirstPoint(currentFrame), generateSecondPoint(currentFrame));
	}
	

	/** How first point will be generated */
	public abstract ValuePoint generateFirstPoint(AIInfoFrame currentFrame);
	

	/** How second point will be generated */
	public abstract ValuePoint generateSecondPoint(AIInfoFrame currentFrame);
	

	/** The method a point shall be evaluated, represented by a float */
	public abstract float evaluatePair(ValuePointPair valuePoints, AIInfoFrame currentFrame);
	
}
