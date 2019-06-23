/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.11.2010
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;


/**
 * This Memory class is used to find an optimal Point to do - well - something with. As shown in the class
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role},
 * an inner class with a specialized Memory is implemented in order to use it.
 * 
 * This memory will generate new points every time you ask it to via {@link APointMemory#generateBestPoint}, but a rule
 * how
 * to generate a single point (just any point that might be viable, as maybe a point on the goalline) and a rule of
 * how to evaluate that point has to be implemented in the extending specialized memory.
 * 
 * The Memory then generates {@link APointMemory#triesPerCycle} points, evaluates them, puts them in order and returns
 * the best to
 * the calling method. Internally, it will save the best {@link APointMemory#memorySize} points, and take these into
 * consideration
 * in the next cycle.
 * 
 * @author GuntherB
 * 
 */

public abstract class APointMemory
{
	
	private final int						memorySize;
	private final int						triesPerCycle;
	
	private static final float			IMPROVEMENT_VALUE	= 1.2f;
	
	private final List<ValuePoint>	bestPoints;
	
	
	/**
	 * Extends a memory that will generate good points depending on your algorithms in generateNewPoint and evaluatePoint
	 * @param memorySize
	 * @param triesPerCycle
	 */
	public APointMemory(int memorySize, int triesPerCycle)
	{
		final WorldFrameFactory wfFactory = new WorldFrameFactory();
		this.memorySize = memorySize;
		this.triesPerCycle = triesPerCycle;
		
		bestPoints = new ArrayList<ValuePoint>();
		
		bestPoints.add(new ValuePoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter()));
		
		for (int c = 1; c < this.memorySize; c++)
		{
			bestPoints.add(generateNewPoint(wfFactory.createFakeAIInfoFrame()));
		}
		
	}
	
	
	/**
	 * This is the only method a calling class will actively call. A new set of random points will be generated and the
	 * best will be returned
	 * 
	 * @param currentFrame
	 * @return
	 */
	public ValuePoint generateBestPoint(AIInfoFrame currentFrame)
	{
		// points from last cycle
		for (ValuePoint point : bestPoints)
		{
			point.setValue(evaluatePoint(point, currentFrame));
		}
		
		// old best point
		ValuePoint oldBest = bestPoints.get(0);
		
		Collections.sort(bestPoints, ValuePoint.VALUELOWCOMPARATOR);
		
		// new points
		for (int counter = 0; counter < triesPerCycle; counter++)
		{
			ValuePoint newPoint = generateNewPoint(currentFrame);
			newPoint.setValue(evaluatePoint(newPoint, currentFrame));
			
			int index = 0;
			for (ValuePoint point : bestPoints)
			{
				if (newPoint.getValue() > point.getValue())
				{
					break;
				}
				index++;
			}
			
			// insert at given index and delete last element
			bestPoints.add(index, newPoint);
			bestPoints.remove(bestPoints.size() - 1);
			
		}
		
		ValuePoint newBest = bestPoints.get(0);
		
		if (((oldBest.getValue() * IMPROVEMENT_VALUE) > newBest.getValue())
				|| (GeoMath.distancePP(oldBest, newBest) < 10))
		{
			bestPoints.add(0, oldBest);
			bestPoints.remove(bestPoints.size() - 1);
			return new ValuePoint(oldBest.x, oldBest.y * 0.8f, oldBest.getValue());
		}
		return new ValuePoint(newBest.x, newBest.y * 0.8f, newBest.getValue());
	}
	
	
	/**
	 * The method a point shall be evaluated, represented by a float
	 * @param valuePoint
	 * @param currentFrame
	 * @return
	 */
	public abstract float evaluatePoint(ValuePoint valuePoint, AIInfoFrame currentFrame);
	
	
	/**
	 * How a single point will be generated
	 * @param currentFrame
	 * @return
	 */
	public abstract ValuePoint generateNewPoint(AIInfoFrame currentFrame);
}
