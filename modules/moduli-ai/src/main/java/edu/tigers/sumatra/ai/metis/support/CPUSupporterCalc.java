/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2016
 * Author(s): Chris
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.support;

import java.util.LinkedList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author ChrisC
 */
public class CPUSupporterCalc extends ACalculator
{
	@Configurable(comment = "Number of gridfields in x direction")
	private static int			numx									= 15;
	
	@Configurable(comment = "Number of gridfields in y direction (determenine pointdistance")
	private static int			numy									= 15;
	
	@Configurable(comment = "Margin for Grid inside field")
	private static double		fieldMargin							= 100;
	
	@Configurable(comment = "Radius around the ball where no Supporter should be")
	private static double		forbiddenSupporterBallRadius	= 800;
	
	@Configurable(comment = "Radius around the ball where the Supporter should be")
	private static double		SupporterBallRadiusFactor		= 0.8;
	
	@Configurable(comment = "Sigma (Widht) of bellshapedcurved for distanceValue")
	private static double		distanceSigma						= 1000;
	
	@Configurable(comment = "disired distance for distanceValue")
	private static double		disiredDistance					= 5000;
	
	private List<ValuePoint>	ballDistancePoints				= new LinkedList<>();
	private List<ValuePoint>	scoreChancePoints					= new LinkedList<>();
	
	private double					distanceBetweenPointsX;
	private double					distanceBetweenPointsY;
	
	
	/**
	 * 
	 */
	public CPUSupporterCalc()
	{
	}
	
	
	private void initialGrid()
	{
		ballDistancePoints.clear();
		scoreChancePoints.clear();
		
		distanceBetweenPointsX = (((Geometry.getFieldLength() / 2) - fieldMargin) / numx);
		distanceBetweenPointsY = (((Geometry.getFieldWidth()) - (fieldMargin * 2)) / numy);
		double yOffset = (Geometry.getFieldWidth() / 2) - fieldMargin;
		double xOffset = (Geometry.getFieldLength() / 2) - fieldMargin;
		
		// Fill first gridPoints
		for (int x = 0; x < numx; x++)
		{
			double curX = xOffset - (distanceBetweenPointsX * x);
			for (int y = 0; y <= numy; y++)
			{
				IVector2 pos = new Vector2(curX, yOffset - (distanceBetweenPointsY * y));
				if (!Geometry.getPenaltyAreaTheir().isPointInShape(pos, 200))
				{
					ballDistancePoints.add(new ValuePoint(pos));
					scoreChancePoints.add(new ValuePoint(pos));
				}
			}
		}
		
		yOffset += distanceBetweenPointsY / 2;
		xOffset += distanceBetweenPointsX / 2;
		
		// Fill gaps in the grid
		for (int x = 1; x <= numx; x++)
		{
			double curX = xOffset - (distanceBetweenPointsX * x);
			for (int y = 1; y <= numy; y++)
			{
				IVector2 pos = new Vector2(curX, yOffset - (distanceBetweenPointsY * y));
				if (!Geometry.getPenaltyAreaTheir().isPointInShape(pos, 200))
				{
					ballDistancePoints.add(new ValuePoint(pos));
					scoreChancePoints.add(new ValuePoint(pos));
				}
			}
		}
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		initialGrid();
		
		for (int i = 0; i < ballDistancePoints.size(); i++)
		{
			scoreChancePoints.get(i).setValue(
					ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), scoreChancePoints.get(i), false));
			ballDistancePoints.get(i).setValue(calcDistanceToBallValue(ballDistancePoints.get(i), baseAiFrame));
		}
		newTacticalField.getScoreChancePoints().clear();
		newTacticalField.getScoreChancePoints().addAll(scoreChancePoints);
		newTacticalField.getBallDistancePoints().clear();
		newTacticalField.getBallDistancePoints().addAll(ballDistancePoints);
	}
	
	
	private double calcDistanceToBallValue(final IVector2 point, final BaseAiFrame aiFrame)
	{
		// e^-(x-verschiebung)^2/sigma^2
		double distance = GeoMath.distancePP(point, aiFrame.getWorldFrame().getBall().getPos());
		return Math.exp(-Math.pow((distance - disiredDistance), 2) / Math.pow(distanceSigma, 2));
	}
}
