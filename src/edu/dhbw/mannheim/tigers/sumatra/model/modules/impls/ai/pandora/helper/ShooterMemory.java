/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * This class can be used to get a good scoring target
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author JanE
 * 
 */
public class ShooterMemory
{
	private IVector2				bestpoint						= null;
	private Float					bestPointValue					= null;
	private BotID					botID;
	
	private final ValuePoint[]	myGeneratedGoalPoints		= generateValuePoints();
	
	private static final float	MAX_DIST_TO_BALL_DEST_LINE	= 500;
	/** weight in the value point for distance between ball-dest-line and foe bot. higher value= less weight */
	private static final int	DIST_BALL_DEST_LINE_WEIGHT	= 5;
	private static final float	VAL_EQUAL_TOL					= 0.3f;
	
	
	/**
	  * 
	  */
	public ShooterMemory()
	{
	}
	
	
	/**
	 * @param currentFrame
	 * @param botID
	 */
	public ShooterMemory(AIInfoFrame currentFrame, BotID botID)
	{
		this.botID = botID;
		update(currentFrame);
	}
	
	
	/**
	 * Update this shooter memory. The best point will be recalculated
	 * 
	 * @param currentFrame
	 */
	public final void update(AIInfoFrame currentFrame)
	{
		bestpoint = evaluateValuePoints(myGeneratedGoalPoints, currentFrame);
	}
	
	
	private IVector2 evaluateValuePoints(ValuePoint[] valuePoints, AIInfoFrame currentFrame)
	{
		final WorldFrame worldFrame = currentFrame.worldFrame;
		// final TrackedTigerBot myBot = currentFrame.worldFrame.tigerBotsVisible.getWithNull(botID);
		// if (myBot == null)
		// {
		// return bestpoint;
		// }
		ValuePoint evalPoint = new ValuePoint(0, 0, 1);
		
		List<List<ValuePoint>> equalPointsList = new LinkedList<List<ValuePoint>>();
		equalPointsList.add(new LinkedList<ValuePoint>());
		
		for (ValuePoint valPoint : valuePoints)
		{
			IVector2 targetPoint = valPoint;
			// will check if there are points on the enemys goal, not being blocked by bots.
			if (GeoMath.p2pVisibility(currentFrame.worldFrame, currentFrame.worldFrame.ball.getPos(), targetPoint,
					(float) ((AIConfig.getGeometry().getBallRadius() * 2) + 0.1)))
			{
				// free visibility
				valPoint.setValue(0.0f);
			} else
			{
				valPoint.setValue(0.5f);
			}
			float elVal = valPoint.getValue();
			Collection<TrackedBot> allBots = new ArrayList<TrackedBot>(worldFrame.foeBots.values());
			allBots.addAll(worldFrame.tigerBotsVisible.values());
			for (final TrackedBot bot : allBots)
			{
				float ownDist = GeoMath.distancePP(currentFrame.worldFrame.ball.getPos(), valPoint);
				float enemyDist = GeoMath.distancePP(bot.getPos(), valPoint);
				if (enemyDist < ownDist)
				{
					// evaluate the generated points: If the view to a point is unblocked the function
					// will get 100 points. Afterwards the distance between the defender and the line between
					// start and target will be added as 1/6000
					float relDist = (GeoMath.distancePL(bot.getPos(), worldFrame.ball.getPos(), valPoint) / MAX_DIST_TO_BALL_DEST_LINE);
					if (relDist > 1)
					{
						relDist = 1;
					} else if (relDist < 0)
					{
						relDist = 0;
					}
					elVal += (1 - relDist) / DIST_BALL_DEST_LINE_WEIGHT;
				}
			}
			valPoint.setValue(elVal);
			
			if (valPoint.getValue() > 1f)
			{
				valPoint.setValue(1f);
			} else if (valPoint.getValue() < 0)
			{
				valPoint.setValue(0f);
			}
			
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
			
			
			currentFrame.tacticalInfo.getGoalValuePoints().add(valPoint);
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
				evalPoint = most.get(most.size() / 2);
			}
		}
		
		bestPointValue = evalPoint.getValue();
		return new Vector2(evalPoint.x(), evalPoint.y());
	}
	
	
	private ValuePoint[] generateValuePoints()
	{
		float ballsize = AIConfig.getGeometry().getBallRadius();
		float goalwidth = AIConfig.getGeometry().getGoalSize();
		int numPoints = (int) (goalwidth / ballsize) - 1;
		ValuePoint pointsArray[] = new ValuePoint[numPoints];
		
		float xCoordinateGoal = AIConfig.getGeometry().getGoalTheir().getGoalPostLeft().x();
		float yCoordinateGoal = AIConfig.getGeometry().getGoalTheir().getGoalPostLeft().y() - ballsize;
		// generate the points
		for (int i = 0; i < pointsArray.length; i++)
		{
			float nY = yCoordinateGoal - (i * ballsize);
			ValuePoint coordsPoints = new ValuePoint(xCoordinateGoal, nY);
			pointsArray[i] = coordsPoints;
		}
		
		return pointsArray;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IVector2 getBestPoint()
	{
		return bestpoint;
	}
	
	
	/**
	 * 
	 * @return Value of the best point [0..1]
	 */
	public Float getBestPointValue()
	{
		return bestPointValue;
	}
	
	
	/**
	 * @return the botID
	 */
	public final BotID getBotID()
	{
		return botID;
	}
	
	
	/**
	 * @param botID the botID to set
	 */
	public final void setBotID(BotID botID)
	{
		this.botID = botID;
	}
	
}
