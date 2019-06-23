/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This class can be used to get a good scoring target
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author JanE
 */
public class ShooterMemory
{
	private ValuePoint				bestpoint			= null;
																	
	private List<ValuePoint>		generatedGoalPoints;
											
	private static final double	VAL_EQUAL_TOL		= 0.3;
																	
	@Configurable(comment = "set the factor how much the best point get's repelled from the keeper")
	private static double			keeperRepellant	= 0.3333333;
																	
																	
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
		ValuePoint evalPoint = new ValuePoint(Geometry.getGoalTheir().getGoalCenter(), 1);
		
		List<List<ValuePoint>> equalPointsList = new LinkedList<List<ValuePoint>>();
		equalPointsList.add(new LinkedList<ValuePoint>());
		
		generatedGoalPoints = generateValuePoints();
		for (ValuePoint valPoint : generatedGoalPoints)
		{
			double value = AiMath.getScoreForStraightShot(wFrame, origin, valPoint);
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
				ITrackedBot keeper = wFrame.getFoeBot(baseAiFrame.getKeeperFoeId());
				if (keeper != null)
				{
					double keeperPosY = keeper.getPos().y();
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
		double ballsize = Geometry.getBallRadius();
		double goalwidth = Geometry.getGoalSize();
		int numPoints = (int) (goalwidth / ballsize) - 1;
		List<ValuePoint> pointsArray = new ArrayList<ValuePoint>(numPoints);
		
		double xCoordinateGoal = Geometry.getGoalTheir().getGoalPostLeft().x();
		double yCoordinateGoal = Geometry.getGoalTheir().getGoalPostLeft().y() - ballsize;
		// generate the points
		for (int i = 0; i < numPoints; i++)
		{
			double nY = yCoordinateGoal - (i * ballsize);
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
