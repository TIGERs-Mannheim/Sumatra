/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class can be used to get a good scoring target
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author JanE
 */
public class ShooterMemory
{
	private ValuePoint					bestpoint			= null;
	
	private final List<ValuePoint>	generatedGoalPoints;
	
	private static final float			VAL_EQUAL_TOL		= 0.3f;
	
	@Configurable(comment = "set the factor how much the best point get's repelled from the keeper")
	private static float					keeperRepellant	= 0.3333333f;
	
	
	/**
	  * 
	  */
	public ShooterMemory()
	{
		generatedGoalPoints = generateValuePoints();
		bestpoint = new ValuePoint(generatedGoalPoints.get(generatedGoalPoints.size() / 2));
	}
	
	
	/**
	 * Update this shooter memory. The best point will be recalculated
	 * 
	 * @param wFrame
	 * @param baseAiFrame
	 * @param origin where is the origin? e.g. ball or bot
	 */
	public final void update(final WorldFrame wFrame, final BaseAiFrame baseAiFrame, final IVector2 origin)
	{
		bestpoint = evaluateValuePoints(wFrame, baseAiFrame, origin);
	}
	
	
	private ValuePoint evaluateValuePoints(final WorldFrame wFrame, final BaseAiFrame baseAiFrame, final IVector2 origin)
	{
		ValuePoint evalPoint = new ValuePoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), 1);
		
		List<List<ValuePoint>> equalPointsList = new LinkedList<List<ValuePoint>>();
		equalPointsList.add(new LinkedList<ValuePoint>());
		
		for (ValuePoint valPoint : generatedGoalPoints)
		{
			float value = AiMath.getScoreForStraightShot(wFrame, origin, valPoint);
			valPoint.setValue(value);
			
			// search for max and return the vector2 as evalpoint
			if (SumatraMath.isEqual(valPoint.getValue(), evalPoint.getValue(), VAL_EQUAL_TOL))
			{
				equalPointsList.get(equalPointsList.size() - 1).add(valPoint);
			} else if (valPoint.getValue() < evalPoint.getValue())
			{
				equalPointsList.clear();
				equalPointsList.add(new LinkedList<ValuePoint>());
			} else
			{
				equalPointsList.add(new LinkedList<ValuePoint>());
			}
			if (valPoint.getValue() < evalPoint.getValue())
			{
				evalPoint.setValue(valPoint.getValue());
				evalPoint.setX(valPoint.x());
				evalPoint.setY(valPoint.y());
			}
		}
		if (!equalPointsList.isEmpty())
		{
			List<ValuePoint> most = equalPointsList.get(0);
			for (int i = 1; i < equalPointsList.size(); i++)
			{
				if (equalPointsList.get(i).size() > most.size())
				{
					most = equalPointsList.get(i);
				}
			}
			if (!most.isEmpty())
			{
				TrackedBot keeper = wFrame.getFoeBot(baseAiFrame.getKeeperFoeId());
				if (keeper != null)
				{
					float keeperPosY = keeper.getPos().y();
					evalPoint = most.get((int) (most.size() * keeperRepellant));
					if (evalPoint.y() < keeperPosY)
					{
						evalPoint = most.get((int) (most.size() * (1 - keeperRepellant)));
					}
				} else
				{
					evalPoint = most.get(most.size() / 2);
				}
			}
		}
		
		return new ValuePoint(evalPoint);
	}
	
	
	private List<ValuePoint> generateValuePoints()
	{
		float ballsize = AIConfig.getGeometry().getBallRadius();
		float goalwidth = AIConfig.getGeometry().getGoalSize();
		int numPoints = (int) (goalwidth / ballsize) - 1;
		List<ValuePoint> pointsArray = new ArrayList<ValuePoint>(numPoints);
		
		float xCoordinateGoal = AIConfig.getGeometry().getGoalTheir().getGoalPostLeft().x();
		float yCoordinateGoal = AIConfig.getGeometry().getGoalTheir().getGoalPostLeft().y() - ballsize;
		// generate the points
		for (int i = 0; i < numPoints; i++)
		{
			float nY = yCoordinateGoal - (i * ballsize);
			ValuePoint coordsPoints = new ValuePoint(xCoordinateGoal, nY);
			pointsArray.add(coordsPoints);
		}
		
		return pointsArray;
	}
	
	
	/**
	 * @return
	 */
	public ValuePoint getBestPoint()
	{
		return bestpoint;
	}
	
	
	/**
	 * @return the generatedGoalPoints
	 */
	public List<ValuePoint> getGeneratedGoalPoints()
	{
		return generatedGoalPoints;
	}
}
